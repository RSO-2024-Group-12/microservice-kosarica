INSERT INTO kosarica (id, id_uporabnik, id_izdelek, cena, kolicina, dodano, rezervirano, tenant) VALUES
(100,1, 100, 699.99, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '10 minutes' ,'org1');