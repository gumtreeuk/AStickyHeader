package dev.dworks.libs.astickyheader;

public class Section {

    protected static final int TYPE_HEADER = 1;
    protected static final int TYPE_HEADER_FILLER = 2;
    protected static final int TYPE_GRID_FILLER = 3;

    private int position;
    private int sectionedPosition;
    private CharSequence title;
    private int type = 0;
    private boolean isSticky = true;
    private int layoutId;

    private Section(int position) {
        this.position = position;
    }

    public CharSequence getTitle() {
        return title;
    }

    public int getPosition() {
        return position;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isHeader() {
        return type == TYPE_HEADER;
    }

    private void setSticky(boolean sticky) {
        this.isSticky = sticky;
    }

    public boolean isSticky() {
        return isSticky;
    }

    public int getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    public int getSectionedPosition() {
        return sectionedPosition;
    }

    public void setSectionedPosition(int sectionedPosition) {
        this.sectionedPosition = sectionedPosition;
    }

    public void setTitle(CharSequence title) {
        this.title = title;
    }

    public static class Builder {

        private final Section section;

        public Builder(int position) {
            section = new Section( position );
        }

        public Builder withLayoutId(int layoutId) {
            section.setLayoutId( layoutId );
            return this;
        }

        public Builder sticky(boolean sticky) {
            section.setSticky(sticky);
            return this;
        }

        public Builder withHeaderText(CharSequence title) {
            section.setTitle( title );
            return this;
        }

        public Builder type(int type) {
            section.setType( type );
            return this;
        }

        public Section build() {
            return section;
        }

    }
}
