browse '/uSDLC/Actors/WebUI/WebDriver'
check empty 'TextField'

enter
    form: 'TestForm'
    TextField: 'test field entry'
    CheckBoxName: true
    'Radio1': 'Radio2'
    'SelectField': 'Two'
check 'TextField', 'test field entry'
check 'CheckBoxName', true
check 'Radio1', 'Radio2'
check only 'SelectField', 'Two'
check all 'SelectField', 'Two'
check some 'SelectField', ['One','Two']
check none 'SelectField', 'Three'