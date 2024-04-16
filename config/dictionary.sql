SET ROLE tq_admin;


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
	lc_word text NOT NULL
);


CREATE INDEX IF NOT EXISTS idx_id ON public.dictionary (id);
CREATE INDEX IF NOT EXISTS idx_wd ON public.dictionary (word);
CREATE INDEX IF NOT EXISTS idx_lcwd ON public.dictionary (lc_word);

CREATE TABLE IF NOT EXISTS public.synonyms (
	id 		BIGINT PRIMARY KEY NOT NULL REFERENCES  public.dictionary (id),
	syn_id	BIGINT NOT NULL REFERENCES  public.dictionary (id)
);

CREATE INDEX IF NOT EXISTS synidx_id ON public.synonyms (id,syn_id);
