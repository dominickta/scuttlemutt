package com.scuttlemutt.app.connection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import backend.scuttlemutt.Scuttlemutt
import com.scuttlemutt.app.MainViewModel
import com.scuttlemutt.app.SingletonScuttlemutt
import com.scuttlemutt.app.components.JetchatAppBar

class ConnectionsFragment : Fragment()  {

    private val TAG = "ConnectionsFragment"
    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var mutt: Scuttlemutt

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mutt = SingletonScuttlemutt.getInstance()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(inflater.context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setContent {
            ConnectionContent(mainViewModel, onNavIconPressed = {mainViewModel.openDrawer()})
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionContent(
    mainViewModel: MainViewModel,
    onNavIconPressed: () -> Unit = { }
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        JetchatAppBar(
            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
            onNavIconPressed = onNavIconPressed,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Channel name
                    Text(
                        text = "Connections",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            actions = {}
        )
        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            DiscoveredConnections(mainViewModel)
            PendingConnections()
            EstablishedConnections()
        }
    }
}

@Composable
fun DiscoveredConnections(
    mainViewModel: MainViewModel
) {
    GroupHeader(name = "Discovered Connections")
    Text(
        text = "Tap to start connection",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(start = 16.dp)
    )
    val icon = Icons.Outlined.WifiFind
    val liveString = mainViewModel.mainLiveData.observeAsState().value!!
    GroupItem(name = liveString, icon)
    GroupItem(name = "Bob", icon, Modifier.clickable (onClick = {mainViewModel.updateLiveString("Bob")}))
    GroupItem(name = "Charlie", icon, Modifier.clickable (onClick = {mainViewModel.updateLiveString("Charlie")}))
}

@Composable
fun PendingConnections() {
    GroupHeader(name = "Pending Connections")
    val icon = Icons.Outlined.Pending
    val onAccept = {} // TODO: call acceptConnection(endpoint)
    val onReject = {} // TODO: call rejectConnection(endpoint)
    GroupItem(name = "Danielle", icon)
    AuthCode("0358", onAccept, onReject)
    GroupItem(name = "Erica", icon)
    AuthCode("8763", onAccept, onReject)
    GroupItem(name = "Fred", icon)
    AuthCode("5894", onAccept, onReject)
}

@Composable
fun AuthCode(code: String, onAccept : () -> Unit, onReject : () -> Unit) {
    Row(
        modifier = Modifier
            .height(24.dp)
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Authentication Code: $code",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 12.dp)
        )
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            tint = Color(0xFF009900),
            modifier = Modifier
                .padding(end = 6.dp)
                .clickable(onClick = onAccept),
            contentDescription = null
        )
        Icon(
            imageVector = Icons.Outlined.Cancel,
            tint = Color(0xFFCC0000),
            modifier = Modifier
                .clickable(onClick = onReject),
            contentDescription = null
        )
    }
}

@Composable
fun EstablishedConnections() {
    GroupHeader(name = "Established Connections")
    val icon = Icons.Outlined.Wifi
    GroupItem(name = "Greg", icon)
    GroupItem(name = "Hanna", icon)
    GroupItem(name = "Isabelle", icon)
}

@Composable
fun GroupHeader(name: String) {
    Row(
        modifier = Modifier
            .heightIn(min = 36.dp)
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 0.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun GroupItem(name: String, icon : ImageVector, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(30.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
            contentDescription = null
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
