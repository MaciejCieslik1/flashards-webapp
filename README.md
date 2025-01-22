# Temat projektu: Aplikacja webowa do nauki z wykorzystaniem fiszek

### Zespół
- Julia Czosnek
- Kacper Górski
- Marcin Polewski
- Maciej Cieślik

### Technologie
- **Spring** – backend, obsługa baz danych
- **React** – frontend
- **Node.js** – obsługa niektórych procesów, API
- **Docker** – konteneryzacja aplikacji
- **Git** – kontrola wersji i współpraca zespołowa
- **MySQL** - bazy danych

### Funkcjonalności Aplikacji

1. **Logowanie i Rejestracja**
   - Logowanie za pomocą konta i hasła
   - Rejestracja przez email
   - Odzyskiwanie hasła
   - Logowanie za pomocą OAuth

2. **Tworzenie Fiszek**
   - Tworzenie folderów na fiszki
   - Modyfikacja i usuwanie fiszek
   - Obsługa trybów powtarzania materiału: Klasyczne fiszki
   - Import i eksport fiszek

3. **Algorytm Powtarzania – Spaced Repetition**
   - Algorytm zaplanowany jako serwis, który planuje pojawianie się fiszek zgodnie z metodą „spaced repetition”

4. **Statystyki**
   - Śledzenie postępów użytkownika
   - Statystyki użytkownika, takie jak:
     - Ilość przejrzanych fiszek w ciągu dnia
     - Ilość fiszek do przejrzenia

5. **Zarządzanie Fiszkami**
   - Zamiana przodu fiszki z tyłem
   - Przechowywanie danych w bazie danych

## Część bazodanowa

Schematy ER i model relacyjny są umieszone odpowiednio w plikach <em>er_model.png</em> i <em>relational_model.png</em>.

### Komendy do uruchamiania aplikacji
- **docker compose down -v --rmi all** wyłącza kontenery, usuwa pamięć i obrazy
- ***docker compose up** uruchamia aplikacje

### Uruchamiania skryptów w bazie danych
1. Uruchomić dockera.
2. Łączenie się z bazą dzięki dodaniu portu 3306 do docker-compose
3. Logowanie poprzez hasło springstudent
4. Odpalenie skryptu w IDE (np. IntelliJ)

### Analiza krytyczna bazy danych

Przedstawione rozwiązanie bazy danych wykazuje solidne podstawy projektowe i spełnia wiele kluczowych wymagań dla systemu zarządzania aplikacją Flashcards. W szczególności:

- **Dobrze zorganizowana struktura danych**: Projekt opiera się na dobrze przemyślanym modelu relacyjnym, który uwzględnia wiele funkcjonalności aplikacji, takich jak zarządzanie użytkownikami, ich statystykami, powiadomieniami czy strukturą folderów. Tabele są logicznie podzielone, co zapewnia przejrzystość i skalowalność.

- **Kluczowe relacje i integralność danych**: Zdefiniowano liczne klucze obce, co pomaga utrzymać integralność referencyjną między tabelami. Dzięki temu rozwiązanie minimalizuje ryzyko niespójności w danych, np. usunięcia użytkownika bez usunięcia jego powiązanych rekordów.

- **Obsługa użytkowników i uprawnień**: Implementacja użytkownika <em>springstudent</em> oraz odpowiednie przydzielenie uprawnień to praktyczny krok w stronę kontroli dostępu i testowania bazy w środowisku symulującym rzeczywiste wykorzystanie.

- **Elastyczność i rozszerzalność**: Wiele tabel (np. User_Preferences, Flashcards_Progresses) uwzględnia możliwość personalizacji lub przechowywania danych użytkownika w sposób łatwo rozszerzalny. Takie podejście sprzyja rozwojowi aplikacji i wprowadzaniu nowych funkcji w przyszłości.

- **Dobre praktyki projektowe**: Przyjęto konwencję nadawania tabelom nazw w liczbie mnogiej oraz stosowania intuicyjnych nazw kolumn, co ułatwia orientację w strukturze bazy. Dodatkowo uwzględniono domyślne wartości dla kluczowych pól (enabled, account_locked), co zmniejsza ryzyko błędów w aplikacji.

- **Przemyślana logika biznesowa**: Zdefiniowanie tabel takich jak Friendships, Notifications, czy Review_Logs wskazuje na kompleksowe podejście do modelowania funkcjonalności, takich jak zarządzanie relacjami między użytkownikami, notyfikacjami i postępami w nauce.

Obszary do ewentualnej poprawy:

- **Optymalizacja indeksów**: Chociaż zdefiniowano unikalny indeks dla adresów e-mail (customers_email_unique), brak dodatkowych indeksów może spowolnić zapytania w tabelach o dużej liczbie rekordów, takich jak Review_Logs czy Flashcards.

- **Lepsza dokumentacja**: Niektóre tabele (np. Folder_Parent, Folders_Decks) mogłyby skorzystać z bardziej rozbudowanej dokumentacji w kodzie, aby lepiej wyjaśnić ich znaczenie i logikę użycia.

- **Niepełne zarządzanie danymi wrażliwymi**: password_hash w tabeli Customers jest przechowywane jako VARCHAR. Nie ma wskazówek dotyczących szyfrowania lub hashowania.

- **Implementacja większej liczby wyzwalaczy**, aby zminimalizować ryzyko problemów z integralnością bazy danych.

Rozwiązanie jest dobrze przemyślane i odpowiednio dostosowane do specyfikacji aplikacji.  Wprowadzenie drobnych usprawnień, takich jak optymalizacja indeksów czy dopracowanie triggerów, mogłoby jeszcze bardziej zwiększyć jej wydajność i elastyczność.