package nl.bneijt.unitrans.blockstore;

public enum BlockExtension {
    DEFAULT(""),
    META(".meta");
    public final String extension;

    BlockExtension(String extension) {
        this.extension = extension;
    }

}
