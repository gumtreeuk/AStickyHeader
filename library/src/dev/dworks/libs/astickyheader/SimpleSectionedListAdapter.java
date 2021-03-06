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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;

import dev.dworks.libs.astickyheader.ui.PinnedSectionListView.PinnedSectionListAdapter;

public class SimpleSectionedListAdapter extends BaseAdapter implements PinnedSectionListAdapter {

    private boolean mValid = true;
    private LayoutInflater mLayoutInflater;
    private ListAdapter mBaseAdapter;
    private SparseArray<Section> mSections = new SparseArray<Section>();

    public SimpleSectionedListAdapter(Context context,
                                      BaseAdapter baseAdapter) {
        mLayoutInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        mBaseAdapter = baseAdapter;
        mBaseAdapter.registerDataSetObserver( new DataSetObserver() {
            @Override
            public void onChanged() {
                mValid = !mBaseAdapter.isEmpty();
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                mValid = false;
                notifyDataSetInvalidated();
            }
        } );
    }

    public void setSections(Section[] sections) {
        mSections.clear();
        notifyDataSetChanged();
        Arrays.sort( sections, new Comparator<Section>() {
            @Override
            public int compare(Section o, Section o1) {
                return (o.getPosition() == o1.getPosition()) ?
                       0 : ((o.getPosition() < o1.getPosition()) ? -1 : 1);
            }
        } );

        int offset = 0; // offset positions for the headers we're adding
        for (Section section : sections) {
            section.setSectionedPosition( section.getPosition() + offset );
            mSections.append( section.getSectionedPosition(), section );
            ++offset;
        }
        notifyDataSetChanged();
    }

    public int sectionedPositionToPosition(int sectionedPosition) {
        if (isSectionHeaderPosition( sectionedPosition )) {
            return ListView.INVALID_POSITION;
        }

        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt( i ).getSectionedPosition() > sectionedPosition) {
                break;
            }
            --offset;
        }
        return sectionedPosition + offset;
    }

    public boolean isSectionHeaderPosition(int position) {
        return mSections.get( position ) != null;
    }

    @Override
    public int getCount() {
        return (mValid ? mBaseAdapter.getCount() + mSections.size() : 0);
    }

    @Override
    public Object getItem(int position) {
        return isSectionHeaderPosition( position )
               ? mSections.get( position )
               : mBaseAdapter.getItem( sectionedPositionToPosition( position ) );
    }

    @Override
    public long getItemId(int position) {
        return isSectionHeaderPosition( position )
               ? Integer.MAX_VALUE - mSections.indexOfKey( position )
               : mBaseAdapter.getItemId( sectionedPositionToPosition( position ) );
    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition( position )
               ? getViewTypeCount() - 1
               : mBaseAdapter.getItemViewType( position );
    }

    @Override
    public boolean isEnabled(int position) {
        //noinspection SimplifiableConditionalExpression
        return isSectionHeaderPosition( position )
               ? false
               : mBaseAdapter.isEnabled( sectionedPositionToPosition( position ) );
    }

    @Override
    public int getViewTypeCount() {
        return mBaseAdapter.getViewTypeCount() + 1; // the section headings
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean hasStableIds() {
        return mBaseAdapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return mBaseAdapter.isEmpty();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (isSectionHeaderPosition( position )) {
            Section s = mSections.get( position );
            TextView view;
            if (null == convertView) {
                convertView = mLayoutInflater.inflate( s.getLayoutId(), parent, false );
            } else {
                if (null == convertView.findViewById( s.getTextViewId() )) {
                    convertView = mLayoutInflater.inflate( s.getLayoutId(), parent, false );
                }
            }
            view = (TextView) convertView.findViewById( s.getTextViewId() );
            view.setText( mSections.get( position ).getTitle() );
            return convertView;
        } else {
            return mBaseAdapter.getView( sectionedPositionToPosition( position ), convertView, parent );
        }
    }

    @Override
    public boolean isItemViewTypePinned(int position) {
        return isSectionHeaderPosition( position );
    }

    protected ListAdapter getWrappedAdapter() {
        return mBaseAdapter;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        if (mBaseAdapter != null) {
            mBaseAdapter.registerDataSetObserver( observer );
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (mBaseAdapter != null) {
            mBaseAdapter.unregisterDataSetObserver( observer );
        }
    }
}