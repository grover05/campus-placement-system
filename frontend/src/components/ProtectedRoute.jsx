import React from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/**
 * Guards a route so that only authenticated users with an allowed role
 * can render its children. Anyone else is redirected.
 */
export default function ProtectedRoute({ allowedRoles, children }) {
  const { user } = useAuth()

  if (!user) {
    return <Navigate to="/login" replace />
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />
  }

  return children
}
