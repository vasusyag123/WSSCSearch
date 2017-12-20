function checkAndUncheckAll (frmName, boxName) {
	var columnBox = document.getElementById(boxName);
	var frm = document.getElementById(frmName);
	if(columnBox.checked) {
		for (var i = 0; i < frm.elements.length; i++) {
	 		frm.elements[i].checked = true;
		}
	}else {
		for (var i = 0; i < frm.elements.length; i++) {
	 		frm.elements[i].checked = false;
		}
	}
	 
}

function checkForSelected(frmName) {

	var isCheckboxSelected = false;
	var frm = document.getElementById(frmName);
	for (var i = 0; i < frm.elements.length; i++) {
	 	if(frm.elements[i].checked == true) {
	 		isCheckboxSelected = true;
	 		break;
	 	}
	}
	if(isCheckboxSelected) {
		return true;
	}else {
		alert("Please select atleast one checkbox");
		return false;
	}
}

function selectAll(frmName) {
	var frm = document.getElementById(frmName);
	for (var i = 0; i < frm.elements.length; i++) {
	 	frm.elements[i].checked = true;
	}
	return true;
}

function resetAll(frmName) {
	var frm = document.getElementById(frmName);
	for (var i = 0; i < frm.elements.length; i++) {
	 	frm.elements[i].checked = false;
	}
}