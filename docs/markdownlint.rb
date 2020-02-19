# Style for markdownlint
# https://github.com/markdownlint/markdownlint/blob/master/docs/creating_styles.md

### Enable all rules
all

### Customization

# MD004 - Unordered list style
# Rationale: consistency
rule 'MD004', :style => :dash

# MD007 - Unordered list indentation
# Rationale: consistency with an indent size in the project
rule 'MD007', :indent => 4

# MD013 - Line length
# TODO: Decrease to 120 ?
rule 'MD013', :line_length => 180

# MD026 - Trailing punctuation in header
# Rationale: more readable links, consistency
rule 'MD026', :punctuation => ".,;:!"

### Exclusion

# MD009 - Trailing spaces
# Rationale: too strict
exclude_rule 'MD009'

# MD012 - Multiple consecutive blank lines
# Rationale: too strict
exclude_rule 'MD012'

# MD024 Multiple headers with the same content
# Rationale: false-positive in tabs (e.g. multiple "Known issues" topics)
exclude_rule 'MD024'

# MD032 - Lists should be surrounded by blank lines
# Rationale: false-positive on sentences with ":" inside
exclude_rule 'MD032'

# MD033 - Inline HTML
# Rationale: false-positive on shortcodes
exclude_rule 'MD033'

# MD034 - Bare URL used
# Rationale: false-positive on auto-links
exclude_rule 'MD034'

# MD036 - Emphasis used instead of a header
# Rationale: false-positive on UI-elements description **Menu > Settings**
exclude_rule 'MD036'

# MD041 First line in file should be a top level header
# Rationale: false-positive on headless pages (e.g. menu index)
exclude_rule 'MD041'
