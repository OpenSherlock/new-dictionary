CREATE SEQUENCE IF NOT EXISTS public.topic_id_seq
    AS bigint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TABLE IF NOT EXISTS public.dictionary (
	id BIGINT PRIMARY KEY NOT NULL DEFAULT nextval('public.topic_id_seq'::regclass),
	word text NOT NULL,
	lc_word text NOT NULL,
	PRIMARY KEY (id, word)
);

CREATE INDEX IF NOT EXISTS idx_id ON tqos_dictionary.dictionary (id);
CREATE INDEX IF NOT EXISTS idx_doc ON tqos_dictionary.dictionary (word);
CREATE INDEX IF NOT EXISTS idx_par ON tqos_dictionary.dictionary (lc_word);


