package lila.security

import lila.user.{ Me, User }

object Granter:

  def apply(permission: Permission)(using me: Me): Boolean =
    me.enabled.yes && apply(permission, me.roles)

  def apply(f: Permission.Selector)(using me: Me): Boolean =
    me.enabled.yes && apply(f(Permission), me.roles)

  def opt(f: Permission.Selector)(using me: Option[Me]): Boolean =
    me.fold(false)(apply(f)(using _))

  def is(permission: Permission)(me: Me): Boolean =
    of(permission)(me.user)

  def is(f: Permission.Selector)(me: Me): Boolean =
    of(f)(me.user)

  def of(permission: Permission)(user: User): Boolean =
    user.enabled.yes && apply(permission, user.roles)

  def of(f: Permission.Selector)(user: User): Boolean =
    user.enabled.yes && apply(f(Permission), user.roles)

  def apply(permission: Permission, roles: Seq[String]): Boolean =
    Permission(roles).exists(_ is permission)

  def byRoles(f: Permission.Selector)(roles: Seq[String]): Boolean =
    apply(f(Permission), roles)

  def canGrant(user: Me, permission: Permission): Boolean =
    is(_.SuperAdmin)(user) || {
      is(_.ChangePermission)(user) && Permission.nonModPermissions(permission)
    } || {
      is(_.Admin)(user) && {
        is(permission)(user) || Set[Permission](
          Permission.MonitoredMod,
          Permission.PublicMod
        )(permission)
      }
    }

  def canViewAltUsername(mod: Me, user: User): Boolean =
    is(_.Admin)(mod) || {
      (is(_.CheatHunter)(mod) && user.marks.engine) ||
      (is(_.BoostHunter)(mod) && user.marks.boost) ||
      (is(_.Shusher)(mod) && user.marks.troll)
    }

  def canCloseAlt(using me: Me) = apply(_.CloseAccount) && apply(_.ViewPrintNoIP)
