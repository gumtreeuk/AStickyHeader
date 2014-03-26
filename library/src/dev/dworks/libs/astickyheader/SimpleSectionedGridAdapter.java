/*
 * Copyright 2013 Hari Krishna Dulipudi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.dworks.libs.astickyheader;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import dev.dworks.libs.astickyheader.ui.FillerView;
import dev.dworks.libs.astickyheader.ui.HeaderLayout;
import dev.dworks.libs.astickyheader.ui.PinnedSectionGridView;
import dev.dworks.libs.astickyheader.ui.PinnedSectionGridView.PinnedSectionGridAdapter;

public class SimpleSectionedGridAdapter extends BaseAdapter implements PinnedSectionGridAdapter {

    private boolean valid = true;
    private LayoutInflater layoutInflater;
    private ListAdapter baseAdapter;
    private SparseArray<Section> sections = new SparseArray<Section>();
    private Context context;
    private int numColumns;
    private int width;
    private int columnWidth;
    private int horizontalSpacing;
    private int stretchMode;
    private int requestedColumnWidth;
    private int requestedHorizontalSpacing;
    private PinnedSectionGridView gridView;
    private int gridFillerHeight;

    public SimpleSectionedGridAdapter(Context context, BaseAdapter baseAdapter) {
        layoutInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.baseAdapter = baseAdapter;
        this.context = context;
        this.baseAdapter.registerDataSetObserver( new DataSetObserver() {
            @Override
            public void onChanged() {
                valid = !SimpleSectionedGridAdapter.this.baseAdapter.isEmpty();
                try {
                    notifyDataSetChanged();
                } catch (Exception e) {
                    //
                }
            }

            @Override
            public void onInvalidated() {
                valid = false;
                notifyDataSetInvalidated();
            }
        } );
    }

    public void setGridView(GridView gridView) {
        if (!(gridView instanceof PinnedSectionGridView)) {
            throw new IllegalArgumentException( "Does your grid view extends PinnedSectionGridView?" );
        }
        this.gridView = (PinnedSectionGridView) gridView;
        stretchMode = this.gridView.getStretchMode();
        width = this.gridView.getWidth() - (this.gridView.getPaddingLeft() + this.gridView.getPaddingRight());
        numColumns = this.gridView.getNumColumns();
        requestedColumnWidth = this.gridView.getColumnWidth();
        requestedHorizontalSpacing = this.gridView.getHorizontalSpacing();
    }

    private int getHeaderSize() {
        if (width != gridView.getWidth()) {
            stretchMode = gridView.getStretchMode();
            width = gridView.getWidth() - (gridView.getPaddingLeft() + gridView.getPaddingRight());
            numColumns = gridView.getNumColumns();
            requestedColumnWidth = gridView.getColumnWidth();
            requestedHorizontalSpacing = gridView.getHorizontalSpacing();
        }

        int spaceLeftOver = width - (numColumns * requestedColumnWidth) -
                            ((numColumns - 1) * requestedHorizontalSpacing);
        switch (stretchMode) {
            case GridView.NO_STRETCH:            // Nobody stretches
                width -= spaceLeftOver;
                columnWidth = requestedColumnWidth;
                horizontalSpacing = requestedHorizontalSpacing;
                break;

            case GridView.STRETCH_COLUMN_WIDTH:
                columnWidth = requestedColumnWidth + spaceLeftOver / numColumns;
                horizontalSpacing = requestedHorizontalSpacing;
                break;

            case GridView.STRETCH_SPACING:
                columnWidth = requestedColumnWidth;
                if (numColumns > 1) {
                    horizontalSpacing = requestedHorizontalSpacing +
                                        spaceLeftOver / (numColumns - 1);
                } else {
                    horizontalSpacing = requestedHorizontalSpacing + spaceLeftOver;
                }
                break;

            case GridView.STRETCH_SPACING_UNIFORM:
                columnWidth = requestedColumnWidth;
                horizontalSpacing = requestedHorizontalSpacing;
                width = width - spaceLeftOver + (2 * horizontalSpacing);
                break;
        }
        return width + ((numColumns - 1) * (columnWidth + horizontalSpacing));
    }

    public void setSections(Section[] sections) {
        this.sections.clear();
        sortSections( sections );
        int offset = 0;
        for (int i = 0; i < sections.length; i++) {
            Section section = sections[i];
            int index = section.getPosition() + offset;
            if (section.isSingle()) {
                addSingle( section, index );
                offset++;
            } else {
                int n = addGridFillers( index );
                offset = offset + n;
                index = index + n;
                n = addHeaderFillers( section, index );
                offset = offset + n;
                index = index + n;
                addHeader( section, index );
                offset++;
            }
        }
        notifyDataSetInvalidated();
        notifyDataSetChanged();
    }

    private void sortSections(Section[] sections) {
        Arrays.sort( sections, new Comparator<Section>() {
            @Override
            public int compare(Section o, Section o1) {
                return (o.getPosition() == o1.getPosition())
                       ? 0
                       : ((o.getPosition() < o1.getPosition()) ? -1 : 1);
            }
        } );
    }

    private int addGridFillers(int fromPosition) {
        int n = numColumns * (1 + fromPosition / numColumns) - fromPosition;
        if (n == numColumns) {
            n = 0;
        }
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                Section s = Section.buildGridFiller( fromPosition, fromPosition + i );
                sections.append( s.getSectionedPosition(), s );
            }
        }
        return n;
    }

    private void addHeader(Section section, int fromPosition) {
        Section s = Section.buildHeader( section, fromPosition );
        sections.append( s.getSectionedPosition(), s );
    }

    private void addSingle(Section section, int fromPosition) {
        Section s = Section.buildSingle( section, fromPosition );
        sections.append( s.getSectionedPosition(), s );
    }

    private int addHeaderFillers(Section section, int fromPosition) {
        int n = numColumns - 1;
        if (n == numColumns) {
            n = 0;
        }
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                Section s = Section.buildHeaderFiller( section, fromPosition, fromPosition + i );
                sections.append( s.getSectionedPosition(), s );
            }
        }
        return n;
    }

    public int positionToSectionedPosition(int position) {
        int offset = 0;
        for (int i = 0; i < sections.size(); i++) {
            if (sections.valueAt( i ).getPosition() > position) {
                break;
            }
            ++offset;
        }
        return position + offset;
    }

    public int sectionedPositionToPosition(int sectionedPosition) {
        if (isSectionHeaderPosition( sectionedPosition )) {
            return ListView.INVALID_POSITION;
        }
        int offset = 0;
        for (int i = 0; i < sections.size(); i++) {
            if (sections.valueAt( i ).getSectionedPosition() > sectionedPosition) {
                break;
            }
            --offset;
        }
        return sectionedPosition + offset;
    }

    public boolean isSectionHeaderPosition(int position) {
        return sections.get( position ) != null;
    }

    @Override
    public int getCount() {
        return (valid ? baseAdapter.getCount() + sections.size() : 0);
    }

    @Override
    public Object getItem(int position) {
        return isSectionHeaderPosition( position )
               ? sections.get( position )
               : baseAdapter.getItem( sectionedPositionToPosition( position ) );
    }

    @Override
    public long getItemId(int position) {
        return isSectionHeaderPosition( position )
               ? Integer.MAX_VALUE - sections.indexOfKey( position )
               : baseAdapter.getItemId( sectionedPositionToPosition( position ) );
    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition( position )
               ? getViewTypeCount() - 1
               : baseAdapter.getItemViewType( position );
    }

    @Override
    public boolean isEnabled(int position) {
        //noinspection SimplifiableConditionalExpression
        return isSectionHeaderPosition( position )
               ? false
               : baseAdapter.isEnabled( sectionedPositionToPosition( position ) );
    }

    @Override
    public int getViewTypeCount() {
        return baseAdapter.getViewTypeCount() + 1;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean hasStableIds() {
        return baseAdapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return baseAdapter.isEmpty();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (isSectionHeaderPosition( position )) {
            Section section = sections.get( position );
            switch (section.getType()) {
                case Section.TYPE_SINGLE:
                    convertView = getSingleView( section, parent );
                    break;
                case Section.TYPE_HEADER:
                    convertView = getHeaderView( section, parent );
                    break;
                case Section.TYPE_HEADER_FILLER:
                    convertView = getHeaderFillerView( section, parent );
                    break;
                case Section.TYPE_GRID_FILLER:
                    convertView = getGridFillerView();
                    break;
                default:
                    throw new RuntimeException( "Type : " + section.getType() + " not supported" );
            }
        } else {
            convertView = baseAdapter.getView( sectionedPositionToPosition( position ), convertView, parent );
            int height = convertView.getMeasuredHeight();
            if (height != 0) {
                gridFillerHeight = View.MeasureSpec.makeMeasureSpec( height, View.MeasureSpec.EXACTLY );
            }
        }
        return convertView;
    }

    private Map<Integer, View> cachedAdViews = new HashMap<Integer, View>();

    private View getSingleView(Section section, ViewGroup parent) {
        View singleView;
        if (section.isContainsAd()) {
            singleView = createAndCacheAdView( section, parent );
        } else {
            singleView = makeSureIsCorrectConvertedView( parent, section.getLayoutId() );
        }
        if (section.isContainsAd() && this instanceof AdProviderAdapter) {
            ((AdProviderAdapter) this).injectAd( section, singleView );
        }
        if (singleView instanceof HeaderLayout) {
            log( "Setting height for single" );
            ((HeaderLayout) singleView).setMeasureTarget( gridFillerHeight );
        }
        singleView.setContentDescription( "Single" );
        return singleView;
    }

    private View getHeaderFillerView(Section section, ViewGroup parent) {
        View convertView = makeSureIsCorrectConvertedView( parent, section.getLayoutId() );
        setHeaderSize( section, convertView, 0 );
        View tv = convertView.findViewById( section.getTextViewId() );
        if (tv != null) {
            TextView view = (TextView) tv;
            view.setText( "" );
        }
        convertView.setContentDescription( "Header filler" );
        return convertView;
    }

    private View getHeaderView(Section section, ViewGroup parent) {
        View convertView;
        if (section.getLayoutId() == 0) {
            convertView = makeSureIsCorrectConvertedView( parent, section.getLayoutId() );
        } else {
            if (section.isContainsAd()) {
                convertView = createAndCacheAdView( section, parent );
            } else {
                convertView = makeSureIsCorrectConvertedView( parent, section.getLayoutId() );
            }
        }
        setHeaderSize( section, convertView , getHeaderSize());
        View tv = convertView.findViewById( section.getTextViewId() );
        if (tv != null) {
            TextView view = (TextView) tv;
            view.setText( section.getTitle() );
        }
        if (section.isContainsAd() && this instanceof AdProviderAdapter) {
            convertView.setMinimumWidth( getHeaderSize() );
            ((AdProviderAdapter) this).injectAd( section, convertView );
        }
        convertView.setContentDescription( "Header" );
        return convertView;
    }

    private void setHeaderSize(Section section, View convertView, int width) {
        View h = convertView.findViewById( section.getContainerViewId() );
        if (h != null) {
            HeaderLayout header = (HeaderLayout) h;
            header.setHeaderWidth( width );
        }
    }

    private View createAndCacheAdView(Section section, ViewGroup parent) {
        View view;
        if (cachedAdViews == null || !cachedAdViews.containsKey( section.getLayoutId() )) {
            view = makeSureIsCorrectConvertedView( parent, section.getLayoutId() );
            cachedAdViews.put( section.getLayoutId(), view );
        } else {
            view = cachedAdViews.get( section.getLayoutId() );
        }
        return view;
    }

    private FillerView getGridFillerView() {
        FillerView fillerView = null;
        if (fillerView == null) {
            fillerView = new FillerView( context );
        }
        fillerView.setMeasureTarget( gridFillerHeight );
        fillerView.setContentDescription( "Filler view" );
        return fillerView;
    }

    private View makeSureIsCorrectConvertedView(ViewGroup parent, int layoutId) {
        return layoutInflater.inflate( layoutId, parent, false );
    }

    @Override
    public boolean isItemViewTypePinned(int position) {
        Section section = sections.get( position );
        if (!isSectionHeaderPosition( position )) {
            return false;
        }
        if (!section.isHeader()) {
            return false;
        }
        return section.isSticky();
    }

    protected ListAdapter getWrappedAdapter() {
        return baseAdapter;
    }

    public static interface AdProviderAdapter {

        void injectAd(Section section, View convertView);
    }

    private void log(String message) {
        Log.v( "ASH", message );
    }
}