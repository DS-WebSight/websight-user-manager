package pl.ds.websight.usermanager.util;

public final class PaginationUtil {

    private PaginationUtil() {
        // no instances
    }

    public static long countPages(long itemsCount, long pageSize) {
        return itemsCount % pageSize == 0 ?
                itemsCount / pageSize :
                itemsCount / pageSize + 1;
    }

    public static long getOffset(long pageNumber, long pageSize) {
        long validatedPageNr = pageNumber - 1 > 0 ? pageNumber - 1 : 0L;
        return validatedPageNr * pageSize;
    }

}
