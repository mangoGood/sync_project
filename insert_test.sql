INSERT INTO test_all_types (
    col_tinyint, col_tinyint_unsigned,
    col_smallint, col_smallint_unsigned,
    col_mediumint, col_mediumint_unsigned,
    col_int, col_int_unsigned,
    col_bigint, col_bigint_unsigned,
    col_float, col_double, col_decimal,
    col_bit,
    col_date, col_datetime, col_timestamp, col_time, col_year,
    col_char, col_varchar, col_binary, col_varbinary,
    col_tinytext, col_text, col_mediumtext, col_longtext,
    col_tinyblob, col_blob, col_mediumblob, col_longblob,
    col_enum, col_set, col_json, col_nullable
) VALUES (
    1, 255,
    100, 65535,
    1000, 16777215,
    100000, 4294967295,
    1000000, 18446744073709551615,
    1.5, 2.5, 123.456789,
    255,
    '2026-04-22', '2026-04-22 10:30:45.123', '2026-04-22 10:30:45.123', '10:30:45', 2026,
    'hello', 'world test', UNHEX('48656C6C6F'), UNHEX('576F726C64'),
    'tiny text', 'normal text', 'medium text', 'long text',
    UNHEX('AB'), UNHEX('CDEF'), UNHEX('010203'), UNHEX('FFFEFD'),
    'value1', 'a,b', '{"test": true}', 42
);
