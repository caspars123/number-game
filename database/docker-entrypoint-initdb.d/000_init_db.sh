#!/usr/bin/env bash

psql postgres -c "CREATE USER db_owner WITH PASSWORD 'ChangeMe';"
psql postgres -c "CREATE DATABASE number_game OWNER db_owner;"
