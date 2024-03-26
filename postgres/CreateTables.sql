CREATE TABLE viewer (
  id bigserial PRIMARY key,
  currencycode varchar(3) DEFAULT 'USD',
  inserted_at timestamp WITH time zone DEFAULT timezone('utc'::text, now()) NOT NULL,
  updated_at timestamp WITH time zone DEFAULT timezone('utc'::text, now()) NOT NULL
);

CREATE TABLE media (
  viewer_id bigserial REFERENCES viewer(id),
  media_id varchar(36) NOT NULL,
  cur_volumes smallserial NOT NULL,
  max_volumes smallserial NOT NULL,
  cost NUMERIC(25, 2) DEFAULT 0.00,
  PRIMARY KEY(viewer_id, media_id)
);