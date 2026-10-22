/**
 * Storage limit of the back-end free-text columns (observations, descriptions, reasons), declared
 * as `VARCHAR2(4000 CHAR)`. Measured in UTF-8 bytes so it also holds when the database caps the
 * column at 4000 bytes.
 */
export const TEXT_FIELD_MAX_BYTES = 4000;
