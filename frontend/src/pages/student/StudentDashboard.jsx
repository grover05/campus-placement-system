import React, { useEffect, useState } from 'react'
import api from '../../api/axios'
import Navbar from '../../components/Navbar'

export default function StudentDashboard() {
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState({ branch: '', percentage: '', backlogs: '', graduationYear: '', phone: '' })
  const [drives, setDrives] = useState([])
  const [applications, setApplications] = useState([])
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [resumeFile, setResumeFile] = useState(null)

  const loadAll = async () => {
    try {
      const [profileRes, drivesRes, appsRes] = await Promise.all([
        api.get('/students/me'),
        api.get('/students/drives/eligible'),
        api.get('/students/applications'),
      ])
      setProfile(profileRes.data)
      setForm({
        branch: profileRes.data.branch || '',
        percentage: profileRes.data.percentage ?? '',
        backlogs: profileRes.data.backlogs ?? '',
        graduationYear: profileRes.data.graduationYear ?? '',
        phone: profileRes.data.phone || '',
      })
      setDrives(drivesRes.data)
      setApplications(appsRes.data)
    } catch (err) {
      setError('Failed to load dashboard data')
    }
  }

  useEffect(() => { loadAll() }, [])

  const handleProfileChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const saveProfile = async (e) => {
    e.preventDefault()
    setError(''); setMessage('')
    try {
      await api.put('/students/me', {
        branch: form.branch,
        percentage: parseFloat(form.percentage),
        backlogs: parseInt(form.backlogs, 10),
        graduationYear: parseInt(form.graduationYear, 10),
        phone: form.phone,
      })
      setMessage('Profile updated.')
      loadAll()
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.details?.join(', ') || 'Failed to update profile')
    }
  }

  const uploadResume = async (e) => {
    e.preventDefault()
    if (!resumeFile) return
    setError(''); setMessage('')
    const data = new FormData()
    data.append('file', resumeFile)
    try {
      await api.post('/students/me/resume', data, { headers: { 'Content-Type': 'multipart/form-data' } })
      setMessage('Resume uploaded.')
      loadAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload resume')
    }
  }

  const applyToDrive = async (driveId) => {
    setError(''); setMessage('')
    try {
      await api.post(`/students/drives/${driveId}/apply`)
      setMessage('Applied successfully.')
      loadAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to apply')
    }
  }

  return (
    <div>
      <Navbar />
      <div className="container">
        <h1>Student Dashboard</h1>
        {message && <div className="card" style={{ background: '#dcfce7', color: '#166534' }}>{message}</div>}
        {error && <div className="error-banner">{error}</div>}

        <div className="card">
          <h3>My Profile</h3>
          <form onSubmit={saveProfile}>
            <div>
              <label>Branch</label>
              <input name="branch" required value={form.branch} onChange={handleProfileChange} />
            </div>
            <div>
              <label>Percentage</label>
              <input name="percentage" type="number" step="0.01" min="0" max="100" required value={form.percentage} onChange={handleProfileChange} />
            </div>
            <div>
              <label>Backlogs</label>
              <input name="backlogs" type="number" min="0" required value={form.backlogs} onChange={handleProfileChange} />
            </div>
            <div>
              <label>Graduation Year</label>
              <input name="graduationYear" type="number" required value={form.graduationYear} onChange={handleProfileChange} />
            </div>
            <div>
              <label>Phone</label>
              <input name="phone" value={form.phone} onChange={handleProfileChange} />
            </div>
            <button className="primary" type="submit">Save Profile</button>
          </form>

          <h3 style={{ marginTop: '1.5rem' }}>Resume</h3>
          {profile?.resumeUrl && <p className="muted">Current: {profile.resumeUrl}</p>}
          <form onSubmit={uploadResume}>
            <input type="file" accept=".pdf,.doc,.docx" onChange={(e) => setResumeFile(e.target.files[0])} />
            <button className="secondary" type="submit" style={{ marginTop: '0.5rem' }}>Upload Resume</button>
          </form>
        </div>

        <div className="card">
          <h3>Eligible Drives</h3>
          {drives.length === 0 ? (
            <p className="muted">No open drives match your profile right now.</p>
          ) : (
            <table>
              <thead>
                <tr><th>Company</th><th>Role</th><th>Min %</th><th>Max Backlogs</th><th>Date</th><th></th></tr>
              </thead>
              <tbody>
                {drives.map((d) => (
                  <tr key={d.id}>
                    <td>{d.companyName}</td>
                    <td>{d.title}</td>
                    <td>{d.minPercentage}</td>
                    <td>{d.maxBacklogs}</td>
                    <td>{d.driveDate}</td>
                    <td><button className="primary" onClick={() => applyToDrive(d.id)}>Apply</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="card">
          <h3>My Applications</h3>
          {applications.length === 0 ? (
            <p className="muted">You haven&apos;t applied to any drives yet.</p>
          ) : (
            <table>
              <thead><tr><th>Company</th><th>Role</th><th>Status</th><th>Applied On</th></tr></thead>
              <tbody>
                {applications.map((a) => (
                  <tr key={a.id}>
                    <td>{a.companyName}</td>
                    <td>{a.driveTitle}</td>
                    <td><span className={`badge ${a.status.toLowerCase()}`}>{a.status}</span></td>
                    <td>{new Date(a.appliedAt).toLocaleDateString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  )
}
