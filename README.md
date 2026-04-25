# Aplicație Desktop cu tabele părinte-copil
# lab1 jdbc swing

## descriere
aplicatie desktop realizata in java swing si jdbc pur, folosind sqlite.

## functionalitati
- afisare categorii
- afisare produse pentru categoria selectata
- adaugare produs
- modificare produs
- stergere produs
- refresh date

## structura proiectului
- db
- dao
- model
- ui

## baza de date
baza de date foloseste urmatoarele tabele:
- categorii
- produse
- etichete
- produs_eticheta

## rulare
1. deschideti proiectul in intellij idea
2. rulati comanda:
   ./gradlew run
3. aplicatia va initializa baza de date automat si va porni interfata grafica

## configurarea conexiunii
conexiunea la baza de date se face prin:
jdbc:sqlite:labjdbc.db

## autor
bogdan buruian ilies