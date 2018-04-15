import os
import re
import warnings


BASE_PATH = 'buildcraft_resources/assets/buildcraftcore/compat/buildcraft/guide/en_us'
LANG_PATH = 'buildcraft_resources/assets/buildcraft/lang/en_US.lang'

EXPORT_DIRECTORY = 'build/guide_html/'


def read_translation():

    result = {}
    with open(LANG_PATH) as f:
        for line in f.readlines():
            if '=' not in line:
                continue
            key, value = line.split('=')
            value = value[:-1]  # drop the `\n` in the end

            result[key] = value
    return result


def get_chapter_name(line):
    return re.search(r'<chapter name="(.*?)"/>', line).group(1)


def _md_to_html_tags(string):
    return string.replace('<bold>', '<strong>').replace('</bold>', '</strong>')


def _dict_to_html(article):
    html = '<h1>%s</h1>\n\n' % article['title']

    html += '<p>%s</p>\n' % article['no_lore']

    for section in article['sections']:
        html += '<h2>%s</h2>\n\n' % section['title']
        html += '<p>%s</p>\n' % _md_to_html_tags(section['content']).replace('\n\n', '</p>\n<p>')

    return html


def _md_to_dict(md_text, translations):
    article = {
        'title': re.search(r'<chapter name="(.*?)"/>', md_text).group(1),
        'lore': re.search(r'<lore>\n(.*?)\n</lore>', md_text, re.DOTALL).group(1),
        'no_lore': re.search(r'<no_lore>\n(.*?)\n</no_lore>', md_text, re.DOTALL).group(1)}

    try:
        article['title'] = translations[article['title']]
    except KeyError:
        warnings.warn('The key \'%s\' is not in the lang file' % article['title'])

    sections = []
    current_section = None
    for line in md_text.split('\n'):
        if line.startswith('<chapter name='):
            if current_section is not None:
                current_section['content'] = '\n'.join(current_section['content'])
                sections.append(current_section)
            current_section = {'title': get_chapter_name(line), 'content': []}
        elif line.startswith('<recipes_usages'):
            # todo: convert this into a proper item
            continue  # drop this for now
        else:
            current_section['content'].append(line)
    current_section['content'] = '\n'.join(current_section['content'])
    sections.append(current_section)

    # first section is about <lore> and <no_lore>. Ignore it.
    article['sections'] = sections[1:]
    return article


def get_all_files():

    list_of_files = {}
    for (dirpath, dirnames, filenames) in os.walk(BASE_PATH):
        for filename in filenames:
            if filename.endswith('.md'):
                list_of_files[filename] = os.sep.join([dirpath, filename])
    return list_of_files


translations = read_translation()


all_files = get_all_files()


if not os.path.exists(EXPORT_DIRECTORY):
    os.makedirs(EXPORT_DIRECTORY)

for file_name in all_files:
    path = all_files[file_name]
    with open(path) as f:
        md_text = f.read()

    try:
        html = _dict_to_html(_md_to_dict(md_text, translations))
    except Exception:
        warnings.warn('The file \'%s\' is ill-formatted' % file_name)
        continue

    with open(EXPORT_DIRECTORY + file_name.replace('.md', '.html'), 'w') as f:
        f.write(html)
