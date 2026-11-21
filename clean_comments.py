import os
import re

def clean_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Safely find and replace javadoc blocks
    # A javadoc block starts with /** and ends with */
    # We want to remove it if it contains 'Place at:'
    # [^*] or \*[^/] matches anything inside without crossing */ boundary
    
    # regex for javadoc: /\*\*.*?\*/
    # we can iterate over them using re.finditer and rebuild the string
    
    def repl(match):
        text = match.group(0)
        # If the javadoc has these typical AI markers, remove it completely
        if 'Place at:' in text or '—' in text or 'Overview:' in text or 'Layout:' in text:
            return ''
        return text

    # Remove all standard AI-like block comments
    content = re.sub(r'/\*\*.*?\*/', repl, content, flags=re.DOTALL)

    lines = content.split('\n')
    new_lines = []
    
    skip_next = False
    for i, line in enumerate(lines):
        if skip_next:
            skip_next = False
            continue
            
        # If current line is an AI-like separator
        if '─' in line and line.strip().startswith('//'):
            # Also check if next line is just a word like "Fields" or "Constructors" surrounded by empty lines or separators
            # The safest is just to remove ANY line that is a comment and contains the box-drawing character.
            continue
            
        # Often there's a comment like "// Fields" surrounded by separators. 
        # But removing just the separators is fine. We can also remove "// Fields" if we want, but it's okay.
        # Another common AI pattern: "// ──────────────────────────────────────────────────────"
        
        new_lines.append(line)

    # Re-join
    new_content = '\n'.join(new_lines)
    
    # Clean up multiple blank lines
    new_content = re.sub(r'\n{3,}', '\n\n', new_content)
    # Remove leading blank lines at the top of the file
    new_content = new_content.lstrip('\n')
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)

if __name__ == "__main__":
    src_dir = r"C:\Users\PRASAD\Downloads\HotelManagementSystem\HotelManagementSystem\src\main\java\com\hotelms"
    count = 0
    for root, dirs, files in os.walk(src_dir):
        for file in files:
            if file.endswith('.java'):
                filepath = os.path.join(root, file)
                clean_file(filepath)
                count += 1
    print(f"Processed {count} files.")
