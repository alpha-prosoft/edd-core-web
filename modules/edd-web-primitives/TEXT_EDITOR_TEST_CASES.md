# Text Editor Test Cases (SunEditor 3.3.0)

## Typography defaults

1. Open editor with empty content.
2. Type one line.
3. Verify typed text uses `Amalia` and `14px` by default.
4. Verify font size control shows `14` after typing.

## Font size selector sync

1. Set current line to `22px`.
2. Move caret within that line using mouse and arrow keys.
3. Verify font size control shows `22`.
4. Change to another line with a different size.
5. Verify control updates to that line size.

## Bold/normal stability

1. In a `22px` line, toggle bold on and type text.
2. Toggle bold off and continue typing.
3. Verify size remains `22px`.
4. Verify font size control still shows `22`.

## Mixed formatting traversal

1. Create three lines with sizes `14`, `18`, `26`.
2. Move caret line by line.
3. Verify selector updates to `14`, `18`, `26` accordingly.

## Paste behavior

1. Paste plain text into editor.
2. Verify pasted content is editable and no runtime errors occur.
3. Move caret in pasted line and verify font size selector reflects active style.

## Single table cell copy/paste

1. Create a table with a single cell containing formatted content such as bold/italic text, font size, and color.
2. Select the whole cell using the table selection handles or cell border selection.
3. Copy the cell and paste it into another selected table cell.
4. Verify the target cell content is replaced rather than appended.
5. Verify the pasted cell keeps the copied inline formatting.

## Read-only/edit mode switch

1. Enter text and switch to read-only mode.
2. Switch back to edit mode.
3. Verify text content remains intact.
4. Verify font size selector still tracks the active line.

