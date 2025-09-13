package com.tonicantarella.gymtracker.ui.gym.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.entity.gym.Exercise
import com.tonicantarella.gymtracker.ui.entity.gym.WorkoutSet
import com.tonicantarella.gymtracker.ui.theme.GymTrackerTheme
import com.tonicantarella.gymtracker.utility.EXERCISE_DESCRIPTION_MAX_SIZE
import com.tonicantarella.gymtracker.utility.EXERCISE_NAME_MAX_SIZE
import com.tonicantarella.gymtracker.utility.MAX_SETS
import java.util.UUID

@Composable
fun ViewExercise(
    exercise: Exercise,
    modifier: Modifier = Modifier
) {
    ExerciseCard(
        header = {
            ExerciseHeader(
                headerTitle = {
                    ExerciseTitle(
                        name = {
                            Text(
                                text = exercise.name
                            )
                        },
                        description = {
                            if (exercise.description != null) {
                                Text(
                                    text = exercise.description,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    )
                }
            )
        },
        modifier = modifier
    ) {
        exercise.sets.forEachIndexed { index, set ->
            HorizontalDivider()
            Set(
                set = set,
                setName = "${stringResource(id = R.string.set)} ${index + 1}"
            )
        }
    }
}

@Composable
fun CreateExercise(
    exercise: Exercise,
    onNameChange: (name: String) -> Unit,
    onDescriptionChange: (description: String) -> Unit,
    deleteEnabled: Boolean,
    onDeletePressed: () -> Unit,
    placeholderName: String,
    addSet: () -> Unit,
    onChangeWeight: (setId: UUID, weight: Double) -> Unit,
    onChangeRepetitions: (setId: UUID, repetitions: Int) -> Unit,
    onRemoveSet: (setId: UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    ExerciseCard(
        header = {
            ExerciseHeader(
                headerTitle = {
                    ExerciseTitle(
                        name = {
                            OutlinedTextField(
                                value = exercise.name,
                                onValueChange = {
                                    if (it.length <= EXERCISE_NAME_MAX_SIZE)
                                        onNameChange(it)
                                },
                                placeholder = {
                                    Text(
                                        text = placeholderName
                                    )
                                },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    }
                                )
                            )
                        },
                        description = {
                            OutlinedTextField(
                                value = exercise.description ?: "",
                                onValueChange = {
                                    if (it.length <= EXERCISE_DESCRIPTION_MAX_SIZE)
                                        onDescriptionChange(it)
                                },
                                placeholder = {
                                    Text(
                                        text = "${stringResource(id = R.string.description)} (${
                                            stringResource(
                                                id = R.string.optional
                                            )
                                        })",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                textStyle = MaterialTheme.typography.labelMedium,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    }
                                )
                            )
                        }
                    )
                },
                headerActions = {
                    IconButton(
                        onClick = onDeletePressed,
                        enabled = deleteEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            tint =
                                if (deleteEnabled) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f),
                            contentDescription = stringResource(id = R.string.delete)
                        )
                    }
                }
            )
        },
        actions = {
            AddSetButton(
                onClick = addSet,
                enabled = exercise.sets.size < MAX_SETS
            )
        },
        modifier = modifier
    ) {
        exercise.sets.forEachIndexed { index, set ->
            HorizontalDivider()
            Set(
                set = set,
                setName = "${stringResource(id = R.string.set)} ${index + 1}",
                deletionEnabled = exercise.sets.size > 1,
                onChangeWeight = { onChangeWeight(set.uuid, it) },
                onChangeRepetitions = { onChangeRepetitions(set.uuid, it) },
                onRemoveSet = { onRemoveSet(set.uuid) },
                onCheckSet = {},
                addingSet = true
            )
        }
        HorizontalDivider()
    }
}

@Composable
fun EditExercise(
    exercise: Exercise,
    onNameChange: (name: String) -> Unit,
    onDescriptionChange: (description: String) -> Unit,
    deleteEnabled: Boolean,
    onDeletePressed: () -> Unit,
    placeholderName: String,
    addSet: () -> Unit,
    onCheckSet: (setId: UUID, checked: Boolean) -> Unit,
    onChangeWeight: (setId: UUID, weight: Double) -> Unit,
    onChangeRepetitions: (setId: UUID, repetitions: Int) -> Unit,
    onRemoveSet: (setId: UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var dropdownMenuOpen by remember { mutableStateOf(false) }
    var editingTitle by remember { mutableStateOf(false) }

    var nameTextFieldValue by remember(editingTitle) {
        mutableStateOf(
            TextFieldValue(
                text = exercise.name,
                selection = if (editingTitle) TextRange(exercise.name.length) else TextRange.Zero
            )
        )
    }

    LaunchedEffect(editingTitle) {
        if (editingTitle) focusRequester.requestFocus()
    }

    ExerciseCard(
        header = {
            ExerciseHeader(
                headerTitle = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)))
                            .clickable(
                                enabled = !editingTitle,
                                onClick = { editingTitle = true }
                            )
                    ) {
                        ExerciseTitle(
                            name = {
                                if (editingTitle) {
                                    OutlinedTextField(
                                        value = nameTextFieldValue,
                                        onValueChange = {
                                            nameTextFieldValue = it
                                            if (it.text.length <= EXERCISE_NAME_MAX_SIZE)
                                                onNameChange(it.text)
                                        },
                                        placeholder = {
                                            Text(
                                                text = placeholderName
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboardController?.hide()
                                                focusManager.clearFocus()
                                            }
                                        ),
                                        modifier = Modifier.focusRequester(focusRequester)
                                    )
                                } else {
                                    Text(
                                        text = exercise.name.ifEmpty { placeholderName }
                                    )
                                }
                            },
                            description = {
                                if (editingTitle) {
                                    OutlinedTextField(
                                        value = exercise.description ?: "",
                                        onValueChange = {
                                            if (it.length <= EXERCISE_DESCRIPTION_MAX_SIZE)
                                                onDescriptionChange(it)
                                        },
                                        placeholder = {
                                            Text(
                                                text = "${stringResource(id = R.string.description)} (${
                                                    stringResource(
                                                        id = R.string.optional
                                                    )
                                                })",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboardController?.hide()
                                                focusManager.clearFocus()
                                            }
                                        )
                                    )
                                } else if (exercise.description != null) {
                                    Text(
                                        text = exercise.description,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        )
                    }
                },
                headerActions = {
                    if (editingTitle) {
                        IconButton(
                            onClick = { editingTitle = false }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.edit_off),
                                contentDescription = stringResource(id = R.string.stop)
                            )
                        }
                    } else {
                        Box {
                            IconButton(
                                onClick = { dropdownMenuOpen = !dropdownMenuOpen }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(id = R.string.more)
                                )
                            }
                            DropdownMenu(
                                expanded = dropdownMenuOpen,
                                onDismissRequest = { dropdownMenuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    onClick = {
                                        editingTitle = true
                                        dropdownMenuOpen = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.edit),
                                            contentDescription = stringResource(id = R.string.edit)
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = stringResource(id = R.string.edit)
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    onClick = {
                                        onDeletePressed()
                                        dropdownMenuOpen = false
                                    },
                                    enabled = deleteEnabled,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(id = R.string.delete)
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = stringResource(id = R.string.delete)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        actions = {
            AddSetButton(
                onClick = addSet,
                enabled = exercise.sets.size < MAX_SETS
            )
        },
        modifier = modifier
    ) {
        exercise.sets.forEachIndexed { index, set ->
            HorizontalDivider()
            Set(
                set = set,
                setName = "${stringResource(id = R.string.set)} ${index + 1}",
                deletionEnabled = exercise.sets.size > 1,
                onChangeWeight = { onChangeWeight(set.uuid, it) },
                onChangeRepetitions = { onChangeRepetitions(set.uuid, it) },
                onRemoveSet = { onRemoveSet(set.uuid) },
                onCheckSet = { onCheckSet(set.uuid, it) },
                addingSet = false
            )
        }
        HorizontalDivider()
    }
}

@Composable
private fun ExerciseTitle(
    name: @Composable () -> Unit,
    description: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
        modifier = modifier
    ) {
        name()
        description()
    }
}

@Composable
private fun ExerciseHeader(
    headerTitle: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    headerActions: @Composable () -> Unit = {}
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = dimensionResource(id = R.dimen.padding_large))
                    .padding(top = dimensionResource(id = R.dimen.padding_large))
            ) {
                headerTitle()
            }
            Box(
                modifier = Modifier
                    .padding(top = dimensionResource(id = R.dimen.padding_medium))
            ) {
                headerActions()
            }
        }
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
    }
}

@Composable
private fun AddSetButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(id = R.string.add)
        )
        Text(
            text = stringResource(id = R.string.set)
        )
    }
}

@Composable
private fun ExerciseCard(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    actions: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
        ) {
            header()
            content()
            actions()
        }
    }
}

val testExercise = Exercise(
    uuid = UUID.randomUUID(),
    name = "Bicep curls",
    description = "Remember to warm up!",
    sets = listOf(
        WorkoutSet(
            uuid = UUID.randomUUID(),
            weight = 10.0,
            repetitions = 8
        ),
        WorkoutSet(
            uuid = UUID.randomUUID(),
            weight = 10.0,
            repetitions = 8
        ),
        WorkoutSet(
            uuid = UUID.randomUUID(),
            weight = 10.0,
            repetitions = 8
        )
    )
)

@Preview
@Composable
private fun ViewExercisePreview() {
    GymTrackerTheme {
        ViewExercise(
            exercise = testExercise
        )
    }
}

@Preview
@Composable
private fun CreateExercisePreview() {
    GymTrackerTheme {
        CreateExercise(
            exercise = testExercise,
            onNameChange = {},
            onDescriptionChange = {},
            placeholderName = "${stringResource(id = R.string.exercise)} 1",
            deleteEnabled = false,
            onDeletePressed = {},
            addSet = {},
            onChangeWeight = { _, _ -> },
            onChangeRepetitions = { _, _ -> },
            onRemoveSet = {}
        )
    }
}

@Preview
@Composable
private fun EditExercisePreview() {
    GymTrackerTheme {
        EditExercise(
            exercise = testExercise,
            onNameChange = {},
            onDescriptionChange = {},
            placeholderName = "${stringResource(id = R.string.exercise)} 1",
            deleteEnabled = false,
            onDeletePressed = {},
            addSet = {},
            onCheckSet = { _, _ -> },
            onChangeWeight = { _, _ -> },
            onChangeRepetitions = { _, _ -> },
            onRemoveSet = {}
        )
    }
}