# MUI v9 Date Picker Verification Test Cases

## Background: Migration from v7 to v9

The date picker component was updated from `@mui/x-date-pickers` v7 API to v9 API. The key change was removing legacy props and adopting the new `localeText` API for customizing text labels.

---

## Test Cases

### Test 1: Date Picker Basic Functionality
**What to test:** Date selection works correctly

```
Steps:
1. Open the application in browser
2. Find a date picker component in the UI
3. Click on the date picker input field
4. Verify the calendar dialog opens
5. Click on a date
6. Verify the date is populated in the input field
✅ Expected: Date selected and dialog closes
```

### Test 2: Clear Button Label
**What to test:** Clear button shows "Clear" text (not a default locale text)

```
Steps:
1. Open date picker dialog
2. Select a date
3. Look for a clear/reset button
4. Verify the button text reads "Clear"
✅ Expected: Clear button displays correct label
```

### Test 3: Clear Button Functionality
**What to test:** Clear button clears the selected date

```
Steps:
1. Open date picker dialog
2. Select a date
3. Click the clear button
4. Verify the input field is now empty
✅ Expected: Date cleared successfully
```

### Test 4: Date Picker Constraints
**What to test:** Min/max date constraints work

```
Steps:
1. Open date picker
2. Try to select a date before minDate (should be disabled/unavailable)
3. Try to select a date after maxDate (should be disabled/unavailable)
4. Try to select a date within range (should be available)
✅ Expected: Only valid dates can be selected
```

### Test 5: Date Format
**What to test:** Date displays in dd.MM.yyyy format

```
Steps:
1. Select a date (e.g., January 15, 2024)
2. Verify input field shows "15.01.2024"
✅ Expected: Format is dd.MM.yyyy
```

### Test 6: Helper Text
**What to test:** Helper text and error messages display

```
Steps:
1. Trigger validation error (if applicable)
2. Verify error message displays below date picker
3. Verify helper text displays when provided
✅ Expected: Text displays in correct location
```

### Test 7: Required Field
**What to test:** Required date pickers enforce selection

```
Steps:
1. Open form with required date picker
2. Try to submit without selecting date
3. Verify error message appears
✅ Expected: Field marked as required, error on empty submit
```

### Test 8: Slot Props
**What to test:** Button IDs and slots work correctly

```
Steps:
1. Inspect HTML elements in DevTools
2. Verify these elements exist with correct IDs:
   - #[id]-openPickerButton
   - #[id]-switchViewButton
   - #[id]-previousIconButton
   - #[id]-nextIconButton
✅ Expected: All slot buttons properly rendered
```

---

## Regression Tests

### Test 9: Read-Only Date Picker
```
If RawDatePicker supports read-only mode:
1. Render a date picker with read-only: true
2. Verify it displays the date but cannot be edited
✅ Expected: Date visible but picker cannot be opened
```

### Test 10: Disabled Date Picker
```
1. Render a date picker with disabled: true
2. Verify it cannot be focused or clicked
✅ Expected: Field appears grayed out and inactive
```

### Test 11: Custom Slot Props
```
If your code uses custom slotProps:
1. Pass custom slotProps for textField
2. Verify custom styles/props are applied
✅ Expected: Custom configuration works
```

---

## Browser Console Check

After each test, verify the browser console has:
- ✅ No JavaScript errors
- ✅ No React warnings
- ✅ No MUI deprecation warnings

---

## Performance Test

```
Steps:
1. Open date picker
2. Switch between months (previous/next buttons)
3. Switch between years
4. Note any lag or delayed rendering
✅ Expected: All interactions feel smooth and responsive
```

---

## Accessibility Test

```
Steps:
1. Try navigating with Tab key
2. Verify date inputs are keyboard accessible
3. Verify all buttons are keyboard accessible
4. Check for ARIA labels and roles
✅ Expected: Full keyboard navigation works
```

---

## Test Result Template

```
Test Case: [Name]
Status: ☐ PASS ☐ FAIL ☐ SKIP
Notes: [Any issues or observations]
```

---

## Known Changes (What's Different from v7)

| Aspect | v7 API | v9 API | Change |
|--------|--------|--------|--------|
| Allow same date | `allowSameDateSelection` | N/A (removed) | Feature removed |
| Input mask | `mask="__.__.____"` | N/A (removed) | Auto-formatted |
| Clear text | `clear-text="Clear"` | `localeText.clearButtonLabel` | API changed |
| Clearable | `clearable={true}` | `clearable={true}` | Still supported |
| Format | `format="dd.MM.yyyy"` | `format="dd.MM.yyyy"` | Unchanged |

---

## If You Find Issues

1. **Date picker won't open:** Check if calendar dialog renders in DOM
2. **Clear button missing:** Verify `clearable: true` prop is set
3. **Format wrong:** Check format string matches expected pattern
4. **Min/max not working:** Verify minDate/maxDate props are dates, not strings
5. **localeText not applying:** Check for conflicting theme overrides

---

**Last Updated:** July 8, 2026  
**Version:** MUI v9.2.0 / x-date-pickers v9.8.0

