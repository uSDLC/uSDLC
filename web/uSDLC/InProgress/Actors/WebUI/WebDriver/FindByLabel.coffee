browse '/uSDLC/Actors/WebUI/WebDriver'

enter
    form: 'Test Form'
    'Text Field': 'test field - 2nd time'
    'Check Box': true
    'Radio 2': true
    'Select List': 'Two'
check 'Text Field', 'test field - 2nd time'
check 'Check Box:', true
check 'Radio 2', true
check only 'Select List', 'Two'