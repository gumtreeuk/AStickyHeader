package dev.dworks.libs.astickyheader;

public class Section {

    protected static final int TYPE_HEADER = 1;
    protected static final int TYPE_HEADER_FILLER = 2;
    protected static final int TYPE_GRID_FILLER = 3;

    private int firstPosition;
    private int sectionedPosition;
    private CharSequence title;
    private int type = 0;
    private boolean isSticky;

    public Section(int firstPosition, CharSequence title) {
        this.firstPosition = firstPosition;
        this.title = title;
        this.isSticky = true;
    }

    public Section(int firstPosition, CharSequence title, boolean isSticky) {
        this.firstPosition = firstPosition;
        this.title = title;
        this.isSticky = isSticky;
    }

    public CharSequence getTitle() {
        return title;
    }

    public int getFirstPosition() {
        return firstPosition;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSectionedPosition() {
        return sectionedPosition;
    }

    public void setSectionedPosition(int sectionedPosition) {
        this.sectionedPosition = sectionedPosition;
    }

    public boolean isHeader() {
        return type == TYPE_HEADER;
    }

    public boolean isSticky() {
        return isSticky;
    }
}
