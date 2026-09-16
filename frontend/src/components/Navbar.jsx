import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const homeByRole = {
    STUDENT: '/student',
    TPO: '/tpo',
    RECRUITER: '/recruiter',
  }

  return (
    <div className="navbar">
      <div>
        <Link className="brand" to={user ? homeByRole[user.role] : '/login'}>
          Campus Placement System
        </Link>
      </div>
      <div>
        {user ? (
          <>
            <span style={{ marginRight: '1rem', opacity: 0.85 }}>
              {user.fullName} &middot; {user.role}
            </span>
            <button onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </div>
  )
}
