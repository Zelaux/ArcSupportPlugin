// This is a generated file. Not intended for manual editing.
package com.zelaux.arcplugin.commandParam.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.zelaux.arcplugin.commandParam.psi.CPTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class ArcCommandParamsParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return expression(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(OPTIONAL_PARAM, PARAM, REQUIRED_PARAM),
  };

  /* ********************************************************** */
  // param_list?
  static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    param_list(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id")) return false;
    if (!nextTokenIs(b, "<identifier>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ID, "<identifier>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACKET id variadic? RIGHT_BRACKET
  public static boolean optional_param(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_param")) return false;
    if (!nextTokenIs(b, LEFT_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACKET);
    r = r && id(b, l + 1);
    r = r && optional_param_2(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACKET);
    exit_section_(b, m, OPTIONAL_PARAM, r);
    return r;
  }

  // variadic?
  private static boolean optional_param_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_param_2")) return false;
    variadic(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // optional_param | required_param
  public static boolean param(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "param")) return false;
    if (!nextTokenIs(b, "<param>", LEFT_ARROW, LEFT_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, PARAM, "<param>");
    r = optional_param(b, l + 1);
    if (!r) r = required_param(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // param (SPACE param)*
  public static boolean param_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "param_list")) return false;
    if (!nextTokenIs(b, "<param list>", LEFT_ARROW, LEFT_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAM_LIST, "<param list>");
    r = param(b, l + 1);
    r = r && param_list_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (SPACE param)*
  private static boolean param_list_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "param_list_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!param_list_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "param_list_1", c)) break;
    }
    return true;
  }

  // SPACE param
  private static boolean param_list_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "param_list_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SPACE);
    r = r && param(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_ARROW id variadic? RIGHT_ARROW
  public static boolean required_param(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_param")) return false;
    if (!nextTokenIs(b, "<required param>", LEFT_ARROW)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REQUIRED_PARAM, "<required param>");
    r = consumeToken(b, LEFT_ARROW);
    r = r && id(b, l + 1);
    r = r && required_param_2(b, l + 1);
    r = r && consumeToken(b, RIGHT_ARROW);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // variadic?
  private static boolean required_param_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_param_2")) return false;
    variadic(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // DOTS
  public static boolean variadic(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variadic")) return false;
    if (!nextTokenIs(b, DOTS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOTS);
    exit_section_(b, m, VARIADIC, r);
    return r;
  }

}
