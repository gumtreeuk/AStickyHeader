package dev.dworks.libs.astickyheader;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class DynamicSectionedGridAdapter extends SimpleSectionedGridAdapter {

    public DynamicSectionedGridAdapter(Context context, int sectionResourceId, int headerId,
                                       int headerContainerId, BaseAdapter baseAdapter) {
        super( context, sectionResourceId, headerId, headerContainerId, baseAdapter );
    }

    private Section[] sections;

    @Override
    public void setSections(Section[] sections) {
        this.sections = sections;
        super.setSections( filter( ) );
    }

    public void updateSections() {
        super.setSections( filter() );
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
            if(s.getFirstPosition() <= count) {
                filtered.add( s );
            }
        }
        return filtered.toArray(new Section[]{});
    }
}
