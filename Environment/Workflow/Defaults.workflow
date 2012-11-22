default_actions =
  new: -> mark 'New', assign editor
  edit: -> mark 'Updated', assign editor
  delete: -> mark 'Deleted', assign owner
  pass: -> mark 'Passed', clear assignment
  fail: -> mark 'Failed', assign owner
  default: -> mark target.state, assign owner
  Reject: -> assign editor, requiring note
  Review: -> mark 'Review', assign owner
  Accepted: -> clear assignment, requiring owner
  Issue: -> assign owner, requiring note
  Delete: -> section deleted
  
default_workflow =
  New:
    buttons: 'Delete'
  Updated:
    buttons: 'Review Issue Delete'
  Deleted: 
    buttons: 'Issue'
  Passed: 
    buttons: 'Issue Delete'
  Failed: 
    buttons: 'Issue Delete'
  Reject: 
    buttons: 'Review Issue Delete'
  Review:
    buttons: 'Accepted Reject'
  Accepted:
    buttons: 'Issue Delete'
  default:
    buttons: 'Issue Delete'