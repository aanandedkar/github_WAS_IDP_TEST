package com.example.GitHubActionsQWas.util;

import lombok.Getter;

@Getter
public enum PortalUrl {
  US1 ("https://qualysguard.qualys.com"),
  US2 ("https://qualysguard.qg2.apps.qualys.com"),
  US3 ("https://qualysguard.qg3.apps.qualys.com"),
  US4 ("https://qualysguard.qg4.apps.qualys.com"),
  EU1 ("https://qualysguard.qualys.eu"),
  EU2 ("https://qualysguard.qg2.apps.qualys.eu"),
  EU3 ("https://qualysguard.qg3.apps.qualys.eu"),
  IN1 ("https://qualysguard.qg1.apps.qualys.in"),
  CA1 ("https://qualysguard.qg1.apps.qualys.ca"),
  AE1 ("https://qualysguard.qg1.apps.qualys.ae"),
  UK1 ("https://qualysguard.qg1.apps.qualys.co.uk"),
  AU1 ("https://qualysguard.qg1.apps.qualys.com.au"),
  KSA1 ("https://qualysguard.qg1.apps.qualysksa.com")
  ;

  private final String url;

  PortalUrl(String url) {
    this.url = url;
  }

  public static PortalUrl getByKey(String key) {
    try {
      return valueOf(key.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(e);
    }
  }
}
