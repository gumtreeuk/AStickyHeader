package dev.dworks.libs.astickyheader;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class DynamicSectionedGridAdapter extends SimpleSectionedGridAdapter {

    public DynamicSectionedGridAdapter(Context context, BaseAdapter baseAdapter) {
        super( context, baseAdapter );
    }

    private Section[] sections;

    @Override
    public void setSections(Section[] sections) {
        this.sections = sections;
        super.setSections( filter( ) );
    }

    private Section[] filter() {
        if(getWrappedAdapter() == null) {
            return new Section[]{};
        }
        if(sections == null || sections.length == 0) {
            return new Section[]{};
        }
        int count = getWrappedAdapter().getCount();
        List<Section> filtered = new ArrayList<Section>();
        for(Section s: sections) {
            if(s.getPosition() <= count) {
                filtered.add( s );
            }
        }
        return filtered.toArray(new Section[]{});
    }
}
