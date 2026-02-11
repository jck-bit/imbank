# Liquibase Quick Start Guide

## 🚀 Quick Setup (One-Time)

### Initial Setup

Run this command **once** when you first clone the repository:

```bash
./scripts/setup-hooks.sh
```

This installs the automated workflow:

1. **Git Pre-commit Hook** - Auto-generates migrations when entities change
2. **Automatic Staging** - Migrations are staged with your entity changes
3. **CI/CD Validation** - GitHub Actions checks migrations on every PR

**That's it!** No manual migration management needed.

## 📖 Daily Workflow (2 Simple Steps)

### When You Change an Entity:

```bash
# 1. Edit your entity
vim src/main/java/com/example/imbank/entity/Employee.java
# (Add/modify/remove fields)

# 2. Commit normally
git add src/main/java/com/example/imbank/entity/Employee.java
git commit -m "feat: add phone field to Employee"
```

**That's it!** The pre-commit hook:
- ✅ Detects entity changes
- ✅ Auto-generates migration file
- ✅ Stages migration automatically
- ✅ Both files commit together

---

## 🎯 Common Scenarios

### Adding a Field

```bash
# 1. Edit entity
@Entity
public class Employee {
    private String phoneNumber;  // ← New field
}

# 2. Commit
git add src/main/java/com/example/imbank/entity/Employee.java
git commit -m "feat: add phone to employee"

# Pre-commit hook auto-generates:
# - src/main/resources/db/changelog/migrations/003-auto-generated-[timestamp].yaml
# - Contains addColumn migration
```

### Removing a Field

```bash
# 1. Remove from entity (delete the field)

# 2. Commit
git add src/main/java/com/example/imbank/entity/Employee.java
git commit -m "feat: remove middle name from employee"

# Pre-commit hook auto-generates dropColumn migration
```

### Renaming a Field

```bash
# 1. Rename in entity
@Entity
public class Employee {
    private String emailAddress;  // ← Renamed from 'email'
}

# 2. Commit
git add src/main/java/com/example/imbank/entity/Employee.java
git commit -m "refactor: rename email to emailAddress"

# Pre-commit hook auto-generates migration
# Note: Review the generated file - Liquibase may create drop+add instead of rename
```

### Adding a New Table

```bash
# 1. Create new entity
@Entity
public class Project {
    @Id
    private Long id;
    private String name;
}

# 2. Commit
git add src/main/java/com/example/imbank/entity/Project.java
git commit -m "feat: add Project entity"

# Pre-commit hook auto-generates createTable migration
```

---

## 🛡️ What Happens Behind the Scenes

### When You Commit:

```
git commit
    ↓
Pre-commit hook runs
    ↓
Checks if entities changed
    ↓
If yes → Checks for migration file
    ↓
If missing → BLOCKS commit + shows error
    ↓
If exists → Allows commit ✅
```

### When You Push:

```
git push
    ↓
GitHub Actions runs
    ↓
Validates migration files
    ↓
Checks syntax
    ↓
Runs tests
    ↓
Shows status on PR ✅
```

### When You Deploy:

```
Deploy app
    ↓
App starts
    ↓
Liquibase runs automatically
    ↓
Applies pending migrations
    ↓
Database updated ✅
```

---

## ⚡ Useful Commands

```bash
# Check what's different between entities and database
mvn liquibase:diff

# See pending migrations
mvn liquibase:status

# Test locally (migrations run automatically on startup)
mvn spring-boot:run

# Manually generate migration (if needed)
mvn liquibase:diffChangeLog

# Rollback last migration
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

---

## 🚨 Troubleshooting

### "Pre-commit hook not running"

```bash
# Re-run setup
./scripts/setup-hooks.sh
```

### "Hook installed but not executing"

```bash
# Check if hook is executable
ls -la .git/hooks/pre-commit

# If not, run setup again
./scripts/setup-hooks.sh
```

### "Migration generation failed during commit"

```bash
# Check if liquibase.properties is configured
cat src/main/resources/liquibase.properties

# Test manually
mvn liquibase:diff

# If it works, try commit again
```

### "Migration failed during deployment"

```bash
# Check logs
tail -f logs/imbank.log

# Rollback
mvn liquibase:rollback -Dliquibase.rollbackCount=1

# Fix the migration file and redeploy
```

### "Need to edit auto-generated migration"

```bash
# After commit, find the generated file
ls src/main/resources/db/changelog/migrations/

# Edit it
vim src/main/resources/db/changelog/migrations/003-auto-generated-*.yaml

# Amend the commit
git add src/main/resources/db/changelog/migrations/003-*.yaml
git commit --amend --no-edit
```

---

## 📚 More Details

See [docs/LIQUIBASE_WORKFLOW.md](docs/LIQUIBASE_WORKFLOW.md) for:
- Complete workflow explanation
- Team collaboration guide
- Production deployment strategies
- Rollback procedures
- Best practices

---

## ✅ Benefits of This Setup

✅ **Zero Manual Tracking** - Git + hooks handle everything
✅ **Automated Checks** - Can't forget migrations
✅ **CI/CD Integrated** - Validates before merge
✅ **Team Friendly** - Everyone stays in sync
✅ **Production Ready** - Safe automated deployments

**You never have to manually check for missing migrations again!**
