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
    private int containerViewId;
    private int textViewId;

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

    private void setTextViewId(int textViewId) {
        this.textViewId = textViewId;
    }

    public int getTextViewId() {
        return textViewId;
    }

    public int getContainerViewId() {
        return containerViewId;
    }

    public void setContainerViewId(int containerViewId) {
        this.containerViewId = containerViewId;
    }

    public static class Builder {

        private final Section section;

        public Builder(int position, int layoutId) {
            section = new Section( position );
            section.setLayoutId( layoutId );
        }

        public Builder withContainerViewId(int containerViewId) {
            section.setContainerViewId( containerViewId );
            return this;
        }

        public Builder sticky(boolean sticky) {
            section.setSticky(sticky);
            return this;
        }

        public Builder withHeaderText(int containerViewId, int textViewId, CharSequence title) {
            section.setContainerViewId( containerViewId );
            section.setTextViewId( textViewId );
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

    public static Section buildGridFiller(int fromPosition, int sectionedPosition) {
        Section s = new Section(fromPosition);
        s.setType( Section.TYPE_GRID_FILLER );
        s.setSectionedPosition( sectionedPosition );
        return s;
    }

    public static Section buildHeader(Section original, int fromPosition) {
        Section s = new Section.Builder( fromPosition, original.getLayoutId() )
                .withHeaderText( original.getContainerViewId(), original.getTextViewId(), original.getTitle() )
                .sticky( original.isSticky() )
                .type( original.getType() )
                .containsAd( original.isContainsAd() )
                .build();
        s.setSectionedPosition( fromPosition );
        return s;
    }

    public static Section buildSingle(Section section, int fromPosition) {
        Section s = new Section.Builder( fromPosition, section.getLayoutId() )
                .withContainerViewId( section.getContainerViewId() )
                .sticky( section.isSticky() )
                .type( section.getType() )
                .containsAd( section.isContainsAd() )
                .build();
        s.setSectionedPosition( fromPosition );
        return s;
    }

    public static Section buildHeaderFiller(Section section, int fromPosition, int sectionedPosition) {
        Section s = new Section.Builder( fromPosition, section.getLayoutId() )
                .withContainerViewId( section.getContainerViewId() )
                .type( Section.TYPE_HEADER_FILLER ).build();
        s.setSectionedPosition( sectionedPosition );
        return s;
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
