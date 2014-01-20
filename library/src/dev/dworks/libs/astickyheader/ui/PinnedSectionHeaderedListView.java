package dev.dworks.libs.astickyheader.ui;

import android.content.Context;
import android.util.AttributeSet;
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
    protected int getPinnedShadowPosition() {
        return mPinnedShadow.position + 1;
    }

    @Override
    public ListAdapter getAdapter() {
        ListAdapter a = super.getAdapter();
        if (a == null) {
            return null;
        }
        if (a instanceof HeaderViewListAdapter) {
            return ((HeaderViewListAdapter) a).getWrappedAdapter();
        }
        return a;
    }
}