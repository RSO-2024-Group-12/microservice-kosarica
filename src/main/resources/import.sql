INSERT INTO kosarica (id, id_uporabnik, id_izdelek, cena, kolicina, dodano, rezervirano) VALUES
(100,1, 100, 699.99, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '10 minutes'),
(101, 1, 101, 149.99, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '10 minutes');