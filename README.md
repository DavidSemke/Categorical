# Categorical
## An Android App for Creating Lists From Catalogs

Categorical is a list-making app where users can create, delete, edit, and organize catalog items, 
which can be added to their list.

Catalog items are found in categories made by the user. Currently, it is impossible for categories 
to have child categories.

The app treats a text file - chosen by the user - as its database. All items are stored in this 
text file. The storage format is as follows:
- '.' is used as a separator between categories and their child items.
- Each line represents a single item. The final word (after the final '.') is the name of the item 
described on that line. All prior words are names of that item's ancestor categories.
- Triangular brackets, '<>', encompass a line if the item the line represents is selected for the 
user's list. 

The app is designed to allow the user to edit the text file directly. Follow the above formatting 
rules to do so. Lines using the '.', '<', and '>' characters illegally are ignored by the app.

To update your view in the app after the file has been edited, perform any operation 
(select/add/edit/delete) or refresh the view. An operation will be carried out as long as the 
selected item still exists among the items listed in the edited text file.

If the file chosen to store the items is unavailable to the app and therefore cannot be updated 
(this will be indicated to the user if this is the case), go to Settings - represented by a cog - 
to select a file.
