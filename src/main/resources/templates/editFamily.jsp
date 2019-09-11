<!DOCTYPE HTML>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<head>
<!-- TODO ygiri
	Make table rows / cells or other fields non editable as appropriate
	Add spinner while ajax calls run
	Add button next to the person row in the UI table to bring up a layover to gather additional person information,
		photo, birth date, notes, etc.
 -->
    <title>Edit Family Tree</title>
    <meta charset="UTF-8">

        <style>
            table {
                width: 70%;
                border-collapse: collapse;
            }
            th, td {
                border: 1px solid orange;
                padding: 5px;
  				text-align: left;
  				height: 10px;
  				vertical-align: bottom;
            }
            th {
			  background-color: orange;
			  color: white;
			}
            tr:nth-child(even) {background-color: #f2f2f2;}
        </style>
</head>
<body>
  <a href="viewFamily">View Family Tree</a> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
  <a href="viewExtendedFamily">View Extended Family Tree</a>

<script src="https://code.jquery.com/jquery-3.4.1.js"></script>

<h1>Edit Family Tree</h1>
</p>
            <c:forEach items="${familyData.keySet}" var="personid">
<!--                 <option value="${category.id}" -->
<!--                     <c:if test="${category.id eq selectedCatId}">selected="selected"</c:if> -->
<!--                     > -->
<!--                     ${category.name} -->
<!--                 </option> -->
				${personid}
            </c:forEach>

 	<div style="overflow-x:auto;">
 	       <table id="family-table" align="center">
            <tr>
            	<th><button onclick="addRow()">+</button></th>
            	<th>Id</th>
                <th>First Name</th>
                <th>Last Name</th>
                <th>Gender</th>
                <th>Spouse</th>
                <th>Mother</th>
                <th>Father</th>
            </tr>
        </table>
        <br>
        <button onclick="saveFamily()">Save</button> &nbsp; &nbsp; &nbsp;
        <button onclick="deleteFamily()">Delete Family</button>
	</div>
 	           	
<script>

// get the data populate the table
		$.ajax({
			type: "GET",
			contentType: "application/json",
			url: "/editFamilyData",
// 			data: tdata,
// 			dataType: 'json',
			cache: false,
			timeout: 100000,
			success: function (data) {
				console.log("SUCCESS : ", data);
			},
			error: function (e) {
				console.log("ERROR : ", e);
			}
		});



function deleteRow(o) {
	    r = o.parentNode.parentNode;
	    id = r.cells[1].innerHTML;
	    deletePerson(id);
// 	    console.log("id: ", id);
        r.parentNode.removeChild(r);
   	}
   
	function addRow() {
	  // Get a reference to the table
	  table = document.getElementById("family-table");

	  // Insert a row at the beginning of the table
	  // the 0-row is column headers
	  newRow = table.insertRow(-1);
	  newRow.setAttribute('contenteditable', 'true');
	  // Insert cells
	  // cell0 is the 'delete row' button
	  cell0 = newRow.insertCell(0);
	  cell0.innerHTML = '<button onclick="deleteRow(this)">-</button>';
	  // cell1 is person id
	  cell1 = newRow.insertCell(1);
// 	  cell1.innerHTML = "n-" + (table.rows.length - 1);
	  
	  // first name
	  newRow.insertCell(2);
	  //last name
	  newRow.insertCell(3);
	  // gender
	  cell3 = newRow.insertCell(4);
// 	  cell3.innerHTML = '<select name="gender2">
// 				<option value="">--Please choose an option--</option>
// 				<option value="F">Female</option>
// 				<option value="M">Male</option>
// 				<option value="O">Other</option>
// 			</select>';
	  // gender
	  newRow.insertCell(5);
	  // mother
	  newRow.insertCell(6);
	  //father
	  newRow.insertCell(7);
	  
	  // add person on server and get id
	  addPerson(cell1);
	}
	
	function deletePerson(id) {
		console.log("deleting person with id: ", id);
		$.ajax({
			type: "POST",
			contentType: "application/json",
			url: "/deletePerson",
			data: id,
			dataType: 'json',
			cache: false,
			timeout: 100000,
			success: function (data) {
				console.log("SUCCESS : ", data);
			},
			error: function (e) {
				console.log("ERROR : ", e);
			}
		});
		
	}

	function deleteFamily() {
		console.log("deleting the whole family!");
		$.ajax({
			type: "POST",
			contentType: "application/json",
			url: "/deleteFamily",
			cache: false,
			timeout: 100000,
			success: function (data) {
				console.log("SUCCESS : ", data);
			},
			error: function (e) {
				console.log("ERROR : ", e);
			}
		});
		
	}

	function addPerson() {
		$.ajax({
			type: "POST",
			contentType: "application/json",
			url: "/addPerson",
			cache: false,
			timeout: 100000,
			success: function (data) {
				console.log("SUCCESS : ", data);
				cell1.innerHTML = data;
			},
			error: function (e) {
				console.log("ERROR : ", e);
			}
		});
		
	}
	
	function saveFamily() {
		var tdata = tableData();
		console.log("tdata:", tdata);
		tdata = JSON.stringify(tdata);
		console.log("json tdata:", tdata);
		
		$.ajax({
			type: "POST",
			contentType: "application/json",
			url: "/saveFamilyData",
			data: tdata,
			dataType: 'json',
			cache: false,
			timeout: 100000,
			success: function (data) {
				console.log("SUCCESS : ", data);
			},
			error: function (e) {
				console.log("ERROR : ", e);
			}
		});
		
	}
	
	function tableData() {
// 		var tdata = new Map();
	    var tdata = [];
	    var table = document.getElementById("family-table");
	    // column names
// 	    var names = table.rows[0].cells;
// 	    console.log("names:", names);
	    for (var i = 1, row; row = table.rows[i]; i++) {
    	   //ignore the first column
    	   tdata[i-1] = [];
    	   for (var j = 1, cell; cell = row.cells[j]; j++) {
//     	     tdata.set(names[j].innerHTML, cell.innerHTML);
				tdata[i-1][j-1] = cell.innerHTML;
    	   }  
    	}
	    
	    return tdata;
	}
</script>
  
</body>
</html>