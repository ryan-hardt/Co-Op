let selectedIds = new Set();
let isEditable = true;

$(document).ready(function() {
	//register all selected items
	$(".selectedItem").each(function() {
		selectedIds.add($(this).attr("id"));
	});
	
	//toggle multiSelectBox item selection
	$(".multiSelectBox").on("click", ".multiSelectBoxItem", function(e) {
		//if a checkbox was clicked
		if($(e.target).is(':checkbox')) {
			//if checkbox is being selected
			if($(e.target).is(':checked')) {
				//select item along with it
				selectItem($(this));
			}
			//if checkbox is being deselected
			else {
				//remove checkbox selection (but keep item selected)
				$(e.target).prop('checked', false);
			}
		}
		//if the item row was clicked
		else {
			//deselect selected item
			if($(this).hasClass("selectedItem")) {
				//if the item is not required
				if(!$(this).hasClass("requiredItem")) {
					//remove selection
					deselectItem($(this));
				}
			}
			//select unselected item
			else {
				selectItem($(this));
			}
		}
	});

	//toggle multiSelectBox item selection
	$(".multiSelectBox").on("click", "#exp-col-btn", function(e) {
		if($(".multiSelectBox").find(".hiddenItem").is(":visible")) {
			$(".multiSelectBox").find(".hiddenItem").hide();
			$("#exp-col-btn").html("&#x25BC;");
		} else {
			$(".multiSelectBox").find(".hiddenItem").show();
			$("#exp-col-btn").html("&#x25B2;");
		}
	});
});

function selectItem(item) {
	if(isEditable) {
		var id = $(item).attr("id");
		if (!selectedIds.has(id)) {
			$(item).addClass("selectedItem");
			selectedIds.add(id);
		}
	}
}

function deselectItem(item) {
	if(isEditable) {
		var id = $(item).attr("id");
		$(item).removeClass("selectedItem");
		selectedIds.delete(id);
		//remove checkbox check (if present)
		$(item).find(':checkbox').each(function () {
			$(this).prop('checked', false);
		});
	}
}

function getSelectedMultiSelectBoxIds() {
	return Array.from(selectedIds);
}

function clearSelectedIds() {
	selectedIds.clear();
}

function preventUpdates() {
	isEditable = false;
}