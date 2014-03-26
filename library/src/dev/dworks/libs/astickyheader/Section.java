package dev.dworks.libs.astickyheader;

public class Section {

    public static final int TYPE_HEADER = 1;
    public static final int TYPE_HEADER_FILLER = 2;
    public static final int TYPE_GRID_FILLER = 3;
    public static final int TYPE_SINGLE = 4;

    private int position;
    private int sectionedPosition;
    private CharSequence title;
    private int type = TYPE_HEADER;
    private boolean isSticky = true;
    private int layoutId;
    private boolean containsAd;

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

    public boolean isSingle() {
        return type == TYPE_SINGLE;
    }

    public boolean isContainsAd() {
        return containsAd;
    }

    public void setContainsAd(boolean containsAd) {
        this.containsAd = containsAd;
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

        public Builder containsAd() {
            section.setContainsAd( true );
            return this;
        }

        public Builder containsAd(boolean value) {
            section.setContainsAd( value );
            return this;
        }

        public Section build() {
            return section;
        }
    }

    @Override
    public String toString() {
        return "Section{" +
               "position=" + position +
               ", sectionedPosition=" + sectionedPosition +
               ", title=" + title +
               ", type=" + type +
               ", isSticky=" + isSticky +
               ", layoutId=" + layoutId +
               ", containsAd=" + containsAd +
               '}';
    }
}
