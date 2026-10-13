"""Check production Room-generated schema, v5 migration and ID deletion with SQLite.

Run after assembleDebug. This complements JVM rules tests; it is not Android UI testing.
"""
from pathlib import Path
import re
import sqlite3

root = Path(__file__).resolve().parents[1]
generated = (root / 'app/build/generated/ksp/debug/java/com/kcalfit/app/data/local/AppDatabase_Impl.java').read_text()
source = (root / 'app/src/main/java/com/kcalfit/app/data/local/AppDatabase.kt').read_text()
creates = re.findall(r'db.execSQL\("(CREATE TABLE IF NOT EXISTS `(?:food_entries|master_foods)`[^"\n]+)"\)', generated)
assert len(creates) == 2, 'Build-generated Room schemas missing'
connection = sqlite3.connect(':memory:')
expected = sqlite3.connect(':memory:')
for statement in creates:
    expected.execute(statement)
    connection.execute(statement.replace(', `nutritionKnown` INTEGER NOT NULL DEFAULT 1', ''))

def insert(table, values):
    columns = connection.execute(f'PRAGMA table_info({table})').fetchall()
    complete = {column[1]: values.get(column[1], '' if column[2] == 'TEXT' else 0) for column in columns}
    connection.execute(f'INSERT INTO {table} ({",".join(complete)}) VALUES ({",".join("?" for _ in complete)})', tuple(complete.values()))

for entry_id, meal, day in [(1, 'Breakfast', '2026-10-14'), (2, 'Breakfast', '2026-10-14'), (3, 'Lunch', '2026-10-14'), (4, 'Breakfast', '2026-10-13')]:
    insert('food_entries', dict(id=entry_id, foodName='Rice', mealType=meal, date=day, calories=250,
        quantity='250 g', proteinGrams=5., carbsGrams=55., fatGrams=.5))
insert('food_entries', dict(id=5, foodName='Unknown macros', mealType='Snack', date='2026-10-14', calories=80))
insert('master_foods', dict(id=1, name='Rice', brand='Generic', barcode='890100100002', isCustom=0))
for statement in re.findall(r'db.execSQL\("([^"\n]+)"\)', source):
    connection.execute(statement)

for table in ['food_entries', 'master_foods']:
    assert connection.execute(f'PRAGMA table_info({table})').fetchall() == expected.execute(f'PRAGMA table_info({table})').fetchall()
assert connection.execute('SELECT COUNT(*) FROM food_entries').fetchone()[0] == 5
assert connection.execute('SELECT quantity, proteinGrams, nutritionKnown FROM food_entries WHERE id=1').fetchone() == ('250 g', 5., 1)
assert connection.execute('SELECT nutritionKnown FROM food_entries WHERE id=5').fetchone()[0] == 0
assert connection.execute('SELECT barcode FROM master_foods WHERE id=1').fetchone()[0] is None
connection.execute('DELETE FROM food_entries WHERE id=?', (1,))
totals = connection.execute("SELECT SUM(calories), SUM(proteinGrams), SUM(carbsGrams), SUM(fatGrams) FROM food_entries WHERE date=? AND mealType=?", ('2026-10-14', 'Breakfast')).fetchone()
assert totals == (250, 5., 55., .5), totals
assert connection.execute('SELECT foodName FROM food_entries WHERE id=2').fetchone()[0] == 'Rice'
assert connection.execute('SELECT COUNT(*) FROM food_entries WHERE date=?', ('2026-10-13',)).fetchone()[0] == 1
print('PASS: v5→v6 schemas, IDs/history/quantities preserved, unknown nutrition, synthetic barcode cleanup, single-ID deletion and recalculated totals')
