package dev.dworks.libs.astickyheader.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;

public class PinnedSectionHeaderedListView extends PinnedSectionListView {

    public PinnedSectionHeaderedListView(Context context, AttributeSet attrs) {
        super( context, attrs );
    }

    public PinnedSectionHeaderedListView(Context context, AttributeSet attrs, int defStyle) {
        super( context, attrs, defStyle );
    }

    @Override
    public ListAdapter getAdapter() {
        ListAdapter a = super.getAdapter();
        if (a == null) {
            return null;
        }
        if (a instanceof HeaderViewListAdapter) {
            return new SectionedHeaderViewListAdapter( (HeaderViewListAdapter)a );
        }
        return a;
    }

    public static class SectionedHeaderViewListAdapter implements PinnedSectionListAdapter {
        private HeaderViewListAdapter adapter;

        public SectionedHeaderViewListAdapter(HeaderViewListAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public boolean isItemViewTypePinned(int position) {
            if(position == 0) {
                return false;
            }
            return ((PinnedSectionListAdapter)adapter.getWrappedAdapter()).isItemViewTypePinned( position - 1 );
        }

        @Override
        public boolean areAllItemsEnabled() {
            return adapter.areAllItemsEnabled();
        }

        @Override
        public boolean isEnabled(int i) {
            return adapter.isEnabled( i );
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {
            adapter.registerDataSetObserver( dataSetObserver );
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
            adapter.unregisterDataSetObserver( dataSetObserver );
        }

        @Override
        public int getCount() {
            return adapter.getCount();
        }

        @Override
        public Object getItem(int i) {
            return adapter.getItem( i );
        }

        @Override
        public long getItemId(int i) {
            return adapter.getItemId( i );
        }

        @Override
        public boolean hasStableIds() {
            return adapter.hasStableIds();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return adapter.getView( i, view, viewGroup );
        }

        @Override
        public int getItemViewType(int position) {
            return adapter.getItemViewType( position );
        }

        @Override
        public int getViewTypeCount() {
            return adapter.getViewTypeCount();
        }

        @Override
        public boolean isEmpty() {
            return adapter.isEmpty();
        }
    }
}