{ pkgs, ... }:

{
  packages = [
    pkgs.openjdk25
    pkgs.maven
  ];

  services.postgres = {
    enable = true;
    package = pkgs.postgresql_16;
    listen_addresses = "127.0.0.1";
    port = 5435;
    initialDatabases = [
      { name = "pleasure_db"; }
    ];
    initialScript = "CREATE USER pleasure_user WITH PASSWORD 'pleasure_pass_123' SUPERUSER;";
  };

  env = {
    DB_HOST = "127.0.0.1";
    DB_PORT = "5435";
    DB_NAME = "pleasure_db";
    DB_USERNAME = "pleasure_user";
    DB_PASSWORD = "pleasure_pass_123";
    BOT_COMMANDS_LANG = "ru";
  };

  processes = {
    bot.exec = "mvn clean package -DskipTests && java -jar target/*.jar me.sha425.ourpleasure.Main";
  };
}
