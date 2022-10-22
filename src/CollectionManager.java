public class CollectionManager {
    private static int collectionMenuOption() {
        System.out.print("\r\nCurrent menu: COLLECTIONS\r\n\r\n" +
                "1: View Your Collections (+ Remove Items)\r\n" +
                "2: Create and Fill New Collection\r\n" +
                "3: Add Items to Existing Collection\r\n" +
                Helpers.DELETE + ": ! Delete a Collection !\r\n" +
                "0: Return to Main Menu\r\n" +
                "What would you like to do?: ");
        return Helpers.getOption(3, true);
    }

    public static void collectionMenu() {
        int option;
        while ((option = collectionMenuOption()) != 0) {
            int collectionID = -1;
            switch (option) {
                case 1: { // Your collections
                    showCollections(false);
                    break;
                }
                case 2: { // New collection
                    collectionID = createCollection();
                    if (collectionID == -1) {
                        break;
                    } // Only break if creation failed
                    // Intentional fall-through if successful
                }
                case 3: { // Add items
                    addItems(collectionID);
                    break;
                }
                case Helpers.DELETE: { // Delete collection
                    deleteCollection();
                    break;
                }
            }
        }
    }

    private static int showCollections(boolean select) {
        // TODO Implement showing and selecting collections
        System.out.println("(Collections, select " + select + ")");
        return 1;
    }

    private static int createCollection() {
        // TODO Implement creation of empty collection
        System.out.println("(Create collection)");
        return 1;
    }

    private static void addItems(int collectionID) {
        if (collectionID == -1) {
            collectionID = showCollections(true);
        }
        SearchManager.songSearchLoop(collectionID);
    }

    private static void deleteCollection() {
        int collectionID = showCollections(true);
        // TODO Implement confirmation and deletion
        System.out.println("(Delete collection, ID " + collectionID + ")");
    }
}
