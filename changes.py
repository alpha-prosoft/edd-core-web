#!/usr/bin/env python3
"""
Generate and validate CHANGES.md from git commit history.

Usage:
    ./changes.py                    # Regenerate full CHANGES.md
    ./changes.py --check            # Regenerate and check if needs commit
    ./changes.py --shallow          # Check only last commit
    ./changes.py --shallow --check  # Check last commit and exit with code
"""

import argparse
import subprocess
import sys
from pathlib import Path


def run_git_command(args):
    """Run a git command and return output."""
    result = subprocess.run(
        ["git"] + args,
        capture_output=True,
        text=True,
        check=True
    )
    return result.stdout.strip()


def make_ticket_bold(line):
    """Make ticket IDs bold by wrapping [TICKET-123] with ** **."""
    import re
    # Match [TICKET-123] pattern and wrap with **
    return re.sub(r'(\[[\w-]+\])', r'**\1**', line)


def format_commit(message):
    lines = [line for line in message.strip().split('\n') if 'Change-Id' not in line]
    if not lines:
        return ''
    subject = make_ticket_bold(lines[0])
    lines[0] = subject if subject.startswith('**[') else '### ' + subject
    return '\n'.join(lines).rstrip()


def get_last_commit():
    """Format the latest commit as a changelog entry."""
    return format_commit(run_git_command(["log", "-1", "--pretty=format:%s%n%b"]))


def get_first_entry_from_changes():
    """Extract first commit entry from CHANGES.md."""
    changes_file = Path("CHANGES.md")
    if not changes_file.exists():
        print("Error: CHANGES.md not found")
        sys.exit(1)

    content = changes_file.read_text()
    lines = content.split('\n')

    # Find "## Changes" section
    in_changes = False
    entry_lines = []
    found_first_entry = False

    for i, line in enumerate(lines):
        if line.strip() == "## Changes":
            in_changes = True
            continue

        if in_changes:
            # Skip empty lines before first entry
            if not found_first_entry and not line.strip():
                continue

            # Public commits without ticket prefixes use a Markdown heading.
            if line.startswith(('**[', '### ')) and not found_first_entry:
                found_first_entry = True
                entry_lines.append(line)
            # The next heading ends the first entry.
            elif found_first_entry and line.startswith(('**[', '### ')):
                break
            # Continuation of entry (any line including empty lines)
            elif found_first_entry:
                entry_lines.append(line)

    # Remove trailing empty lines
    while entry_lines and not entry_lines[-1].strip():
        entry_lines.pop()

    return '\n'.join(entry_lines)


def get_all_commits():
    """Get all commit messages since initial commit."""
    initial_commit = Path("initial-public-commit.txt").read_text().strip()
    output = run_git_command([
        "log", "-z", "--pretty=format:%s%n%b", f"{initial_commit}..HEAD"
    ])
    return '\n\n'.join(format_commit(commit) for commit in output.split('\0'))


def regenerate_changes_file():
    """Regenerate CHANGES.md from git history."""
    header = "# Changelog\n\n## Changes\n\n"
    commits = get_all_commits()
    content = header + commits + "\n"

    Path("CHANGES.md").write_text(content)


def has_uncommitted_changes():
    """Check if CHANGES.md has uncommitted changes."""
    result = subprocess.run(
        ["git", "diff", "--name-only", "CHANGES.md"],
        capture_output=True,
        text=True
    )
    return bool(result.stdout.strip())


def main():
    parser = argparse.ArgumentParser(description="Manage CHANGES.md file")
    parser.add_argument("--shallow", action="store_true",
                        help="Check only last commit (for shallow clones)")
    parser.add_argument("--check", action="store_true",
                        help="Exit with code 1 on validation failure")

    args = parser.parse_args()

    if args.shallow:
        # Shallow mode: compare last commit with first entry in CHANGES.md
        try:
            last_commit = get_last_commit()
            first_entry = get_first_entry_from_changes()

            if last_commit == first_entry:
                print("Last commit is already in CHANGES.md")
                sys.exit(0)
            else:
                print("Last commit is NOT in CHANGES.md")
                print()
                print("Expected commit:")
                print(last_commit)
                print()
                print("First entry in CHANGES.md:")
                print(first_entry)

                if args.check:
                    sys.exit(1)
        except subprocess.CalledProcessError as e:
            print(f"Error running git command: {e}")
            sys.exit(1)
    else:
        # Full mode: regenerate CHANGES.md
        if args.check:
            # Check for uncommitted changes BEFORE regenerating
            if has_uncommitted_changes():
                print("Error: CHANGES.md has uncommitted manual changes")
                print("Please commit or revert your changes first")
                sys.exit(1)

        try:
            regenerate_changes_file()

            if args.check:
                # Check if regeneration created changes
                if has_uncommitted_changes():
                    print("CHANGES.md has been updated. Please review the changes and commit them.")
                    sys.exit(1)
                else:
                    print("CHANGES.md is up to date")
        except subprocess.CalledProcessError as e:
            print(f"Error running git command: {e}")
            sys.exit(1)
        except FileNotFoundError as e:
            print(f"Error: {e}")
            sys.exit(1)


if __name__ == "__main__":
    main()
