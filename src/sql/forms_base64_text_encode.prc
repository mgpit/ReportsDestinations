CREATE OR REPLACE FUNCTION FORMS_BASE64_TEXT_ENCODE( aValue IN VARCHAR2 ) RETURN VARCHAR2 IS
BEGIN
    IF ( aValue IS NULL ) THEN
        RETURN NULL;
    END IF;
    
    RETURN utl_raw.cast_to_varchar2( utl_encode.base64_encode( utl_raw.cast_to_raw( aValue ) ) );
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END FORMS_BASE64_TEXT_ENCODE;
/ 