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
    private View lastViewSeen;
    private int numColumns;
    private int width;
    private int columnWidth;
    private int horizontalSpacing;
    private int stretchMode;
    private int requestedColumnWidth;
    private int requestedHorizontalSpacing;
    private PinnedSectionGridView gridView;

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
                notifyDataSetChanged();
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
        int index = 0;
        for (int i = 0; i < sections.length; i++) {
            Section section = sections[i];
            index = section.getFirstPosition() + offset;
            int numberOfGridFiller = numColumns * (1 + index / numColumns) - index;
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
        notifyDataSetChanged();
    }

    private void sortSections(Section[] sections) {
        Arrays.sort( sections, new Comparator<Section>() {
            @Override
            public int compare(Section o, Section o1) {
                return (o.getFirstPosition() == o1.getFirstPosition())
                       ? 0
                       : ((o.getFirstPosition() < o1.getFirstPosition()) ? -1 : 1);
            }
        } );
    }

    private void addGridFillers(int number, int fromPosition) {
        if (number <= 0) {
            return;
        }
        for (int i = 0; i < number; i++) {
            Section s = new Section( fromPosition, null );
            s.setSectionedPosition( fromPosition + i );
            s.setType( Section.TYPE_GRID_FILLER );
            sections.append( s.getSectionedPosition(), s );
        }
    }

    private void addHeader(Section section, int fromPosition) {
        Section s = new Section( section.getFirstPosition(), section.getTitle(), section.isSticky() );
        s.setType( Section.TYPE_HEADER );
        s.setSectionedPosition( fromPosition );
        sections.append( s.getSectionedPosition(), s );
    }

    private void addHeaderFillers(int number, int fromPosition) {
        if (number <= 0) {
            return;
        }
        for (int i = 0; i < number; i++) {
            Section s = new Section( fromPosition, null );
            s.setSectionedPosition( s.getFirstPosition() + i );
            s.setType( Section.TYPE_HEADER_FILLER );
            sections.append( s.getSectionedPosition(), s );
        }
    }

    public int positionToSectionedPosition(int position) {
        int offset = 0;
        for (int i = 0; i < sections.size(); i++) {
            if (sections.valueAt( i ).getFirstPosition() > position) {
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
            switch (sections.get( position ).getType()) {
                case Section.TYPE_HEADER:
                    convertView = getHeaderView( position, convertView, parent );
                    break;
                case Section.TYPE_HEADER_FILLER:
                    convertView = getHeaderFillerView( convertView, parent );
                    break;
                case Section.TYPE_GRID_FILLER:
                    convertView = getGridFillerView( lastViewSeen );
                    break;
                default:
                    throw new RuntimeException( "Type : " + sections.get( position ).getType() + " not supported" );
            }
        } else {
            convertView = baseAdapter.getView( sectionedPositionToPosition( position ), convertView, parent );
            lastViewSeen = convertView;
        }
        return convertView;
    }

    private View getHeaderFillerView(View convertView, ViewGroup parent) {
        convertView = makeSureIsCorrectConvertedView( convertView, parent );
        HeaderLayout header = (HeaderLayout) convertView.findViewById( headerLayoutId );
        TextView view = (TextView) convertView.findViewById( headerId );
        view.setText( "" );
        header.setHeaderWidth( 0 );
        return convertView;
    }

    private View getHeaderView(int position, View convertView, ViewGroup parent) {
        convertView = makeSureIsCorrectConvertedView( convertView, parent );
        HeaderLayout header = (HeaderLayout) convertView.findViewById( headerLayoutId );
        TextView view = (TextView) convertView.findViewById( headerId );
        view.setText( sections.get( position ).getTitle() );
        header.setHeaderWidth( getHeaderSize() );
        return convertView;
    }

    private View makeSureIsCorrectConvertedView(View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = layoutInflater.inflate( sectionResourceId, parent, false );
        } else {
            if (null == convertView.findViewById( headerLayoutId )) {
                convertView = layoutInflater.inflate( sectionResourceId, parent, false );
            }
        }
        return convertView;
    }

    private FillerView getGridFillerView(View lastViewSeen) {
        FillerView fillerView = null;
        if (fillerView == null) {
            fillerView = new FillerView( context );
        }
        fillerView.setMeasureTarget( lastViewSeen );
        return fillerView;
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
}