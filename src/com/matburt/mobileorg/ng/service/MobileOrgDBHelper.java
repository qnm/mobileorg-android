package com.matburt.mobileorg.ng.service;

import org.kvj.bravo7.DBHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class MobileOrgDBHelper extends DBHelper {

	public MobileOrgDBHelper(Context context, String path) {
		super(context, path, 10);
	}

	@Override
	public void migrate(SQLiteDatabase db, int version) {
		switch (version) {
		case 1:
			db.execSQL("CREATE TABLE IF NOT EXISTS files"
					+ " (file VARCHAR, name VARCHAR," + " checksum VARCHAR)");
			db.execSQL("CREATE TABLE IF NOT EXISTS todos"
					+ " (tdgroup int, name VARCHAR," + " isdone INT)");
			db.execSQL("CREATE TABLE IF NOT EXISTS priorities"
					+ " (tdgroup int, name VARCHAR," + " isdone INT)");
			break;
		case 2:
			db.execSQL("drop table if exists files");
			db.execSQL("create table if not exists files (id integer primary key autoincrement, file text, checksum text)");
			db.execSQL("create table if not exists data (id integer primary key autoincrement, parent_id integer, "
					+ "indent integer default 0, editable integer default 0, note_id text, original_id text, "
					+ "type text, priority text, todo text, title text, raw text, tags text, "
					+ "level integer default 0, before text, after text)");
			break;
		case 3:
			db.execSQL("alter table data add file_id integer");
			break;
		case 4:
			db.execSQL("drop table if exists todos");
			db.execSQL("drop table if exists priorities");
			db.execSQL("create table todos (id integer primary key autoincrement, groupnum integer, name text, isdone integer default 0)");
			db.execSQL("create table priorities (id integer primary key autoincrement, name text)");
			break;
		case 5:
			db.execSQL("create table changes (id integer primary key autoincrement, type text, data_id integer, old_value text, new_value text, changed integer)");
			break;
		case 6:
			db.execSQL("alter table files add data_id integer");
			break;
		case 7:
			db.execSQL("alter table data add habit text");
			break;
		case 8:
			db.execSQL("create index if not exists data_parent_id_type on data (parent_id, type)");
			db.execSQL("create index if not exists data_note_id on data (note_id)");
			break;
		case 9:
			db.execSQL("drop index if exists data_note_id");
			break;
		case 10:
			db.execSQL("create table uploads (id integer primary key autoincrement, name text, filename text, data_id integer)");
			break;
		}
	}

}
