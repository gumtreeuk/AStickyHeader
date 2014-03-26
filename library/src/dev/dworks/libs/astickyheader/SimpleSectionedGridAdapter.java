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
    private int sectionResourceId;
    private int headerId;
    private int headerLayoutId;
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

    public SimpleSectionedGridAdapter(Context context, int sectionResourceId, int headerId,
                                      int headerLayoutId, BaseAdapter baseAdapter) {
        layoutInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.sectionResourceId = sectionResourceId;
        this.baseAdapter = baseAdapter;
        this.context = context;
        this.headerId = headerId;
        this.headerLayoutId = headerLayoutId;
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
        Log.v( "XXX", "columns : " + numColumns );
        for (int i = 0; i < sections.length; i++) {
            Section section = sections[i];
            int index = section.getPosition() + offset;
            Log.v( "XXX", "index: " + index );
            if (section.isSingle()) {
                addSingle( section, index );
                offset++;
            } else {
                int numberOfGridFiller = numColumns * (1 + index / numColumns) - index;
                Log.v( "XXX", "number of fillers : " + numberOfGridFiller );
                if (numberOfGridFiller == numColumns) {
                    numberOfGridFiller = 0;
                }
                addGridFillers( numberOfGridFiller, index );
                offset = offset + numberOfGridFiller;
                index = index + numberOfGridFiller;
                int numberOfHeaderFillers = numColumns - 1;
                addHeaderFillers( numberOfHeaderFillers, index );
                offset = offset + numberOfHeaderFillers;
                index = index + numberOfHeaderFillers;
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

    private void addGridFillers(int number, int fromPosition) {
        if (number <= 0) {
            return;
        }
        for (int i = 0; i < number; i++) {
            Log.v( "XXX", "add grid filler " + fromPosition + " sp :" + (fromPosition + i) );
            Section s = new Section.Builder( fromPosition ).type( Section.TYPE_GRID_FILLER ).build();
            s.setSectionedPosition( fromPosition + i );
            sections.append( s.getSectionedPosition(), s );
        }
    }

    private void addHeader(Section section, int fromPosition) {
        Log.v( "XXX", "add header " + fromPosition + " sp: " + fromPosition );
        Section s = new Section.Builder( fromPosition ).withHeaderText( section.getTitle() )
                .sticky( section.isSticky() ).withLayoutId( section.getLayoutId() )
                .type( section.getType() ).containsAd( section.isContainsAd() ).build();
        s.setSectionedPosition( fromPosition );
        sections.append( s.getSectionedPosition(), s );
    }

    private void addSingle(Section section, int fromPosition) {
        Log.v( "XXX", "add single " + fromPosition + " sp: " + fromPosition );
        Section s = new Section.Builder( fromPosition ).withHeaderText( section.getTitle() )
                .sticky( section.isSticky() ).withLayoutId( section.getLayoutId() )
                .type( section.getType() ).containsAd( section.isContainsAd() ).build();
        s.setSectionedPosition( fromPosition );
        sections.append( s.getSectionedPosition(), s );
    }

    private void addHeaderFillers(int number, int fromPosition) {
        if (number <= 0) {
            return;
        }
        for (int i = 0; i < number; i++) {
            Log.v( "XXX", "add header filler " + fromPosition + " sp: " + (fromPosition + i) );
            Section s = new Section.Builder( fromPosition ).type( Section.TYPE_HEADER_FILLER ).build();
            s.setSectionedPosition( s.getPosition() + i );
            sections.append( s.getSectionedPosition(), s );
        }
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
                    convertView = getHeaderFillerView( parent );
                    break;
                case Section.TYPE_GRID_FILLER:
                    convertView = getGridFillerView();
                    break;
                default:
                    throw new RuntimeException( "Type : " + section.getType() + " not supported" );
            }
        } else {
            convertView = baseAdapter.getView( sectionedPositionToPosition( position ), convertView, parent );
            gridFillerHeight = View.MeasureSpec.makeMeasureSpec(
                    convertView.getMeasuredHeight(), View.MeasureSpec.EXACTLY );
        }
        return convertView;
    }

    private Map<Integer, View> cachedAdViews = new HashMap<Integer, View>();

    private View getSingleView(Section section, ViewGroup parent) {
        View singleView;
        if (section.getLayoutId() == 0) {
            return makeSureIsCorrectConvertedView( parent );
        } else {
            if (section.isContainsAd()) {
                singleView = createAndCacheAdView( section, parent );
            } else {
                singleView = makeSureIsCorrectConvertedView( parent, section.getLayoutId() );
            }
        }
        if (section.isContainsAd() && this instanceof AdProviderAdapter) {
            ((AdProviderAdapter) this).injectAd( section, singleView );
        }
        singleView.setContentDescription( "Single" );
        return singleView;
    }

    private View getHeaderFillerView(ViewGroup parent) {
        View convertView = makeSureIsCorrectConvertedView( parent );
        HeaderLayout header = (HeaderLayout) convertView.findViewById( headerLayoutId );
        TextView view = (TextView) convertView.findViewById( headerId );
        view.setText( "" );
        header.setHeaderWidth( 0 );
        convertView.setContentDescription( "Header filler" );
        return convertView;
    }

    private View getHeaderView(Section section, ViewGroup parent) {
        View convertView;
        if (section.getLayoutId() == 0) {
            convertView = makeSureIsCorrectConvertedView( parent );
        } else {
            if (section.isContainsAd()) {
                convertView = createAndCacheAdView( section, parent );
            } else {
                convertView = makeSureIsCorrectConvertedView( parent, section.getLayoutId() );
            }
        }
        HeaderLayout header = (HeaderLayout) convertView.findViewById( headerLayoutId );
        if (header != null) {
            if (section.getTitle() != null) {
                TextView view = (TextView) convertView.findViewById( headerId );
                view.setText( section.getTitle() );
            }
            header.setHeaderWidth( getHeaderSize() );
        }
        if (section.isContainsAd() && this instanceof AdProviderAdapter) {
            convertView.setMinimumWidth( getHeaderSize() );
            ((AdProviderAdapter) this).injectAd( section, convertView );
        }
        convertView.setContentDescription( "Header" );
        return convertView;
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
        Log.v( "XXX", "Measure grid filler: " + gridFillerHeight );
        fillerView.setMeasureTarget( gridFillerHeight );
        fillerView.setContentDescription( "Filler view" );
        return fillerView;
    }

    private View makeSureIsCorrectConvertedView(ViewGroup parent, int layoutId) {
        return layoutInflater.inflate( layoutId, parent, false );
    }

    private View makeSureIsCorrectConvertedView(ViewGroup parent) {
        return makeSureIsCorrectConvertedView( parent, sectionResourceId );
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
}