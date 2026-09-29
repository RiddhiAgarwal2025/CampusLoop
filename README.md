# CampusLoop

**Good Stuff. Second Chances.**

CampusLoop is a peer-to-peer circular marketplace for students. Buy and sell used textbooks,
electronics, calculators, gadgets, hostel essentials and stationery with other students on your
campus. You meet in person, and the buyer pays from a CampusLoop wallet **only after the seller
confirms** the exchange.

> Buy less new. Give more a second life.

---

## 1. Requirements

| Component | Version |
|---|---|
| JDK | 8 or newer (the build targets Java 8 bytecode) |
| Maven | 3.6 or newer |
| Database | MySQL 8.0 (recommended), MySQL 5.7, or MariaDB 10.3+ |
| Servlet container | Apache Tomcat **8.5.x** (Servlet 3.1 / JSP 2.3, `javax.servlet`) |

No other software, CDN or paid service is needed. The app works completely offline.

## 2. Set up the database

```bash
mysql -u root -p < database/campusloop.sql
```

The script:

* drops and recreates the `campusloop` database (utf8mb4), so **it erases any existing CampusLoop data**;
* creates the tables, foreign keys, CHECK/UNIQUE constraints, indexes and the `seller_rating_summary` view;
* loads 7 categories, 3 demo students, 17 listings, 5 exchanges, 3 reviews and a consistent wallet ledger;
* creates the application account `campusloop` / `CampusLoop@2026` with SELECT/INSERT/UPDATE/DELETE rights;
* finishes with three integrity checks, which must all print `0` mismatches.

### Demo accounts (password: `password123`)

| Email | Wallet | Situation in the demo data |
|---|---|---|
| `riddhi@campusloop.local` | ₹3,600 | Seller rated 4.5★; has a pending request for Aarav's headphones |
| `aarav@campusloop.local` | ₹2,150 | Must confirm Riddhi's request; his fridge exchange is confirmed but he is ₹1,050 short |
| `ananya@campusloop.local` | ₹1,750 | Seller rated 5.0★; waiting for Aarav's payment |

## 3. Configure (only if your setup differs)

Settings live in `src/main/resources/campusloop.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/campusloop?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=5000
db.user=campusloop
db.password=CampusLoop@2026
app.timezone=Asia/Kolkata
upload.dir=
upload.maxBytes=5242880
```

Any value can be overridden **without rebuilding**:

* JVM system property: `-Dcampusloop.db.password=secret`
* environment variable: `CAMPUSLOOP_DB_PASSWORD=secret`

For example, to use your MySQL `root` account instead of the created app account, set
`CAMPUSLOOP_DB_USER=root` and `CAMPUSLOOP_DB_PASSWORD=<your password>` before starting Tomcat.

**Uploaded images** are stored *outside* the web application, so they survive redeploys. The
default location is `<your home folder>/campusloop-uploads/{listings,profiles}`. To change it, set
`upload.dir` (or `CAMPUSLOOP_UPLOAD_DIR`). The folder is created automatically on startup.

## 4. Build

```bash
mvn clean package
```

Output: `target/CampusLoop.war`

## 5. Deploy and run

1. Copy `target/CampusLoop.war` into Tomcat's `webapps/` folder.
2. Start Tomcat (`bin/startup.sh`, or `bin\startup.bat` on Windows).
3. Open **http://localhost:8080/CampusLoop/**

Tomcat's log (`logs/catalina.out`) should contain:

```
INFO: CampusLoop connected to the database successfully.
INFO: CampusLoop started. Uploads directory: ...
```

## 6. A 5-minute tour

1. **As a guest:** browse the marketplace, open a product, **Add to Cart**, open the cart.
2. **Log in as Ananya.** Your guest cart moves into your account automatically.
3. **Sell:** upload a photo (required), set a title, price and condition, and publish.
4. **Log in as Aarav:** the dashboard shows a request waiting for your confirmation. Open **Sales** and **Confirm** it.
5. **Log in as Riddhi:** open **Purchases → Pay now**. The wallet is debited, Aarav is credited, the item becomes SOLD, and **Items Recirculated** on the home page goes up by one.
6. **Review** Aarav from Purchases, then open his seller page to see the rating calculated from real reviews.

## 7. How it works

```
PRODUCT → CART → CHECKOUT (location · date · time · note) → AWAITING_SELLER     no money moves
          seller confirms → AWAITING_PAYMENT (item RESERVED, competing requests closed)
          buyer pays      → COMPLETED (one DB transaction: debit, credit, SOLD, carts, points)
          seller rejects / buyer cancels → REJECTED / CANCELLED                   nobody charged
```

* **Wallet (simulated):** top-ups are ₹100–₹10,000 each, with a maximum balance of ₹1,00,000. Every change writes a ledger row in the same DB transaction, so `wallet_balance` always equals the sum of the ledger.
* **Payment:** a single JDBC transaction locks the exchange, the listing and both students (in a fixed order), verifies everything, moves the money, marks the listing SOLD, completes the exchange, clears carts and awards points. Any failure rolls back the whole payment.
* **Impact Points:** each completed resale earns **10 points for the buyer and 10 for the seller** (`ImpactRules.POINTS_PER_COMPLETED_RESALE`). The points measure participation in reuse; they are not a carbon or waste estimate.
* **Items Recirculated** = the number of COMPLETED exchanges, read live from the database.
* **Seller reviews:** only the buyer of a completed exchange can review, once per exchange, never themselves. The rating is the average of real reviews; sellers without reviews are shown as "New seller".

## 8. Architecture

Java Servlets (controllers) → services (business rules, transactions) → DAOs (JDBC, PreparedStatement) → MySQL.
JSP + JSTL views live under `/WEB-INF/views`. Scriptlets are disabled, so no business logic sits in the JSPs.

```
CampusLoop/
├── pom.xml
├── README.md
├── database/campusloop.sql
└── src/main/
    ├── java/com/campusloop/
    │   ├── config/      AppConfig, AppContextListener
    │   ├── controller/  24 servlets (28 routes)
    │   ├── dao/         Student, Category, Listing, Cart, Transaction, Review, Wallet, Stats
    │   ├── filter/      CharacterEncoding, SecurityHeaders, Csrf, Auth, NavContext
    │   ├── model/       entities, enums and page view-models
    │   ├── service/     Auth, Market, Listing, Cart, Wallet, Exchange, Payment, TradeHistory,
    │   │                Review, Dashboard, Profile, ImpactRules, LoginThrottle + exceptions
    │   └── util/        DbUtil, PasswordUtil, SessionUtil, CsrfUtil, CookieUtil, GuestCart,
    │                    ImageStorage, Validator, Params, Format, Redirects, Views
    ├── resources/campusloop.properties
    └── webapp/
        ├── index.jsp                       (forwards to /home)
        ├── META-INF/context.xml            (SameSite=Lax session cookie)
        ├── assets/app.css, js/app.js, images/ (logo, hero, demo illustrations)
        └── WEB-INF/web.xml, tags/, views/
```

### Routes

| Route | Access | Purpose |
|---|---|---|
| `/home`, `/market`, `/product?id=`, `/seller?id=`, `/how-it-works` | public | Browse |
| `/register`, `/login`, `POST /logout` | public | Accounts |
| `/cart`, `POST /cart/add`, `POST /cart/remove` | guests and students | Cart (cookie for guests) |
| `/dashboard`, `/profile` | students | Overview and own profile |
| `/sell`, `/my-listings`, `/edit?id=`, `POST /listing/remove` | owner | Listings |
| `/checkout?listingId=`, `/exchange?id=`, `POST /exchange/{confirm,reject,cancel}`, `POST /pay` | participants | Exchange workflow |
| `/purchases`, `/sales`, `/review?transactionId=`, `/wallet` | students | History, reviews, wallet |
| `/media/*` | public | Uploaded images (path-traversal safe) |

There is no servlet mapped to `/` or `/*`, so `/CampusLoop/assets/app.css` is always served by Tomcat as CSS.

## 9. Security

* **Passwords:** PBKDF2-HMAC-SHA256 (120,000 iterations, random salt), with constant-time comparison.
* **Sign-in:** a generic error message prevents account enumeration, a brute-force lock applies after 5 failures, and the session ID is rotated on login.
* **Sessions and cookies:** sessions are HttpOnly with cookie-only tracking, and every cookie is HttpOnly.
* **Guest cart cookie:** strictly parsed and re-validated against the DB on every read.
* **CSRF:** a synchronizer token is required on every POST.
* **Authorization:** `AuthFilter` covers the protected routes, and every service **also** checks ownership against the session user. IDs in URLs or hidden fields are never trusted.
* **SQL:** every query uses a PreparedStatement, and search text is LIKE-escaped.
* **Output:** all user text is escaped (`<c:out>`/`fn:escapeXml`), backed by a strict Content-Security-Policy with no inline scripts.
* **Uploads:** the file type is detected by magic bytes (JPEG/PNG/WebP), with a 5 MB limit and a pixel-dimension check that doesn't decode the full image. Files get random names, the original filename is never used, and they are stored outside the webapp.
* **Errors:** friendly error pages for 400/403/404/405/413/500; stack traces are never shown.

## 10. Troubleshooting

| Symptom | Fix |
|---|---|
| Error page on every request; the log says "could not connect to the database" | Import `database/campusloop.sql`, then check `db.url`, `db.user` and `db.password` (or the `CAMPUSLOOP_DB_*` environment variables). |
| `Public Key Retrieval is not allowed` | Keep `allowPublicKeyRetrieval=true` in `db.url` (MySQL 8 default authentication). |
| `CREATE USER` fails when importing | Your MySQL password policy is stricter than usual: remove the `CREATE USER`/`GRANT` lines and use `CAMPUSLOOP_DB_USER`/`CAMPUSLOOP_DB_PASSWORD` with an existing account. |
| Image uploads fail | Make sure the Tomcat user can write to the upload directory (see `upload.dir`). |
| Dates or "today" look off | Set `app.timezone` (default `Asia/Kolkata`). |
| MySQL 5.7 | Fully supported. MySQL 5.7 parses but does not enforce CHECK constraints; the application enforces the same rules itself. |

Wallet payments in CampusLoop are simulated for academic use. No real money or payment gateway is involved.
