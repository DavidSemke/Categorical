# DepthDefinedShoppingList
This is a shopping list app made in Android Studio. Users can create/delete/edit their own items and add items to their shopping list.

Items are stored in tree format, where an item may have a child item. The root of the entire item tree is named "CATALOG ROOT". 

The default, and currently immutable, maximum depth of the tree is 1, where items added directly to the tree root are of depth 0. Thus, "depth defined".

The app treats a text file - chosen by the user - as its database. All items are stored in this textfile. The storage format is as follows:
- '.' is used as a separator between items and their child items. 
- Each line represents a single item. The final word (after the final '.') is the name of the item described on that line. All prior words are names of that item's ancestors.
- Triangular brackets, '<>', encompasses a line if the item the line represents is selected for the user's list. 

The app is designed to allow the user to edit the text file directly. Follow the above formatting rules to do so. To update your view in the app after the file has been edited, perform any operation (select/add/edit/delete) or select the refresh button. An operation will be carried out as long as the selected item still exists among the items listed 
in the text file.

If the file chosen to store the items is unavailable to the app and therefore cannot be updated (this will be indicated to the user if this is the case), go to Settings - 
represented by a cog - to select a file.
