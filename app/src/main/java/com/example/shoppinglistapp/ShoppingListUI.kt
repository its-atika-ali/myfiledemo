package com.example.shoppinglistapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.ShoppingCart


data class ShoppingItem(
    var id : Int,
    var name : String,
     var quantity : Int,

    var isEditing : Boolean = false
)
data class Screens(
    val title: String,
    val icon: ImageVector
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombinedMainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val selectedItem = remember { mutableStateOf("Shopping List") }

    // Drawer Items List (moved to correct place)
    val drawerItems = listOf(
        Screens("Home", Icons.Default.Home),
        Screens("Settings", Icons.Default.Settings),
        Screens("Profile", Icons.Default.Person),
        Screens("Shopping List", Icons.Default.ShoppingCart)
    )

    var shoppingItems by remember { mutableStateOf(emptyList<ShoppingItem>()) }
    var newItemName by remember { mutableStateOf("") }
    var newItemQuantity by remember { mutableIntStateOf(1) }
    var showDialog by remember { mutableStateOf(false) }
    var isQuantityEdited by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    Text(
                        text = "Main Menu",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    drawerItems.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(text = item.title) },
                            selected = item.title == selectedItem.value,
                            onClick = {
                                selectedItem.value = item.title
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(vertical = 8.dp),
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = selectedItem.value) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch { drawerState.open() }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            floatingActionButton = {
                if (selectedItem.value == "Shopping List") {
                    FloatingActionButton(onClick = { showDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedItem.value) {
                    "Home" -> HomeScreen(modifier = Modifier.padding(innerPadding))
                    "Profile" -> ProfileScreen(modifier = Modifier.padding(innerPadding))
                    "Settings" -> SettingsScreen(modifier = Modifier.padding(innerPadding))
                    "Shopping List" -> {
                        LazyColumn {
                            items(shoppingItems) { item ->
                                if (item.isEditing) {
                                    EditableShoppingItem(
                                        item = item,
                                        onEditComplete = { name, quantity ->
                                            shoppingItems = shoppingItems.map {
                                                if (it.id == item.id)
                                                    it.copy(name = name, quantity = quantity, isEditing = false)
                                                else it.copy(isEditing = false)
                                            }
                                        }
                                    )
                                } else {
                                    ShoppingListItem(
                                        item = item,
                                        onEdit = {
                                            shoppingItems = shoppingItems.map {
                                                it.copy(isEditing = it.id == item.id)
                                            }
                                        },
                                        onDelete = {
                                            shoppingItems = shoppingItems.filter {
                                                it.id != item.id
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        if (showDialog) {
                            AddItemsDialog(
                                itemName = newItemName,
                                itemQuantity = newItemQuantity.toString(),
                                onItemNameChange = { newItemName = it },
                                onItemQuantityChange = {
                                    newItemQuantity = it.toIntOrNull() ?: 1
                                },
                                onDismiss = {
                                    showDialog = false
                                    isQuantityEdited = false
                                },
                                onConfirm = {
                                    val quantity = newItemQuantity
                                    shoppingItems = shoppingItems + ShoppingItem(
                                        id = shoppingItems.size + 1,
                                        name = newItemName,
                                        quantity = quantity
                                    )
                                    showDialog = false
                                    newItemName = ""
                                    newItemQuantity = 1
                                    isQuantityEdited = false
                                },
                                isQuantityEdited = isQuantityEdited,
                                onQuantityEditedChange = { isQuantityEdited = it }
                            )
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemsDialog(
    itemName : String,

    itemQuantity : String,

    onItemNameChange :(String) ->Unit,
    onItemQuantityChange : (String) -> Unit,

    onDismiss : () -> Unit,
    onConfirm : () -> Unit,

    isQuantityEdited: Boolean,
    onQuantityEditedChange: (Boolean) -> Unit

) {
    AlertDialog( onDismissRequest = onDismiss,
    title = { Text(text = "Add New Item")},

        text = {
            Column {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = onItemNameChange,
                    label = {
                        Text(text = "Item Name")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
        
                OutlinedTextField(
                    value = if (!isQuantityEdited) "" else itemQuantity,
                    onValueChange ={
                        onQuantityEditedChange(true)

                        onItemQuantityChange(it) },
                    label = {
                        Text(text = "Item Quantity")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter quantity") }
                )


            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm

            ) {
                Text(text = "Add")
            }

        }
    )



    }


@Composable
fun ShoppingListItem(


    item: ShoppingItem,
    onEdit: () -> Unit,
    onDelete: () ->Unit


) {
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)

    ){
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =Arrangement.SpaceBetween
        ){
            Column {
                Text(text = item.name,style = MaterialTheme.typography.titleMedium)

                Text(text = "Quantity : ${item.quantity}", style = MaterialTheme.typography.bodyMedium)
            }
            Row {

                IconButton(
                    onClick = onEdit
                ) {
                    Icon( imageVector = Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(
                    onClick = onDelete
                ) {
                    Icon( imageVector = Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }


    
}


@Composable
fun EditableShoppingItem(
    item : ShoppingItem,
    onEditComplete : (String, Int )->Unit
) {
    
    var editedName by remember { mutableStateOf(item.name) }
    var editedQuantity by remember { mutableStateOf(item.quantity.toString()) }



    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)

    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text(text = "Item Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(modifier = Modifier.height(8.dp))


            OutlinedTextField(
                value = editedQuantity,
                onValueChange = { editedQuantity = it },

                label = {Text(text = "Item Quantity")},
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))




            Button(
                onClick = {
                    val quantity = editedQuantity.toIntOrNull() ?: 1
                    onEditComplete(editedName, quantity)

                }
            ) {
                Text(text = "Update")
            }
        }



    }}
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Text(text = "This is Home Screen")

}

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    Text(text = "This is Profile screen")


}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Text(text = "This is settings screen")


}

