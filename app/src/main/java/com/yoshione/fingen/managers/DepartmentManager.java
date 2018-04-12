package com.yoshione.fingen.managers;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

import com.yoshione.fingen.dao.DepartmentsDAO;
import com.yoshione.fingen.model.Department;
import com.yoshione.fingen.R;

/**
 * Created by slv on 08.04.2016.
 *
 */
public class DepartmentManager {

    public static void showEditDialog(final Department department, final Activity activity) {

        String title;
        if (department.getID() < 0) {
            title = activity.getResources().getString(R.string.ent_new_department);
        } else {
            title = activity.getResources().getString(R.string.ent_edit_department);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        final EditText input = (EditText) activity.getLayoutInflater().inflate(R.layout.template_edittext, null);
        input.setText(department.getName());
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                if (!newName.isEmpty()) {
                    DepartmentsDAO departmentsDAO = DepartmentsDAO.getInstance(activity);
                    department.setName(input.getText().toString());
                    try {
                        departmentsDAO.createModel(department);
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
