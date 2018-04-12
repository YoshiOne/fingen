package com.yoshione.fingen.managers;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

import com.yoshione.fingen.dao.ProjectsDAO;
import com.yoshione.fingen.model.Project;
import com.yoshione.fingen.R;

/**
 * Created by slv on 08.04.2016.
 *
 */
public class ProjectManager {

    public static void showEditDialog(final Project project, final Activity activity) {

        String title;
        if (project.getID() < 0) {
            title = activity.getResources().getString(R.string.ent_new_project);
        } else {
            title = activity.getResources().getString(R.string.ent_edit_project);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        final EditText input = (EditText) activity.getLayoutInflater().inflate(R.layout.template_edittext, null);
        input.setText(project.getName());
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                if (!newName.isEmpty()) {
                    ProjectsDAO projectsDAO = ProjectsDAO.getInstance(activity);
                    project.setName(input.getText().toString());
                    try {
                        projectsDAO.createModel(project);
                    } catch (Exception e) {
                        Toast.makeText(activity, R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        builder.show();
        input.requestFocus();
    }
}
